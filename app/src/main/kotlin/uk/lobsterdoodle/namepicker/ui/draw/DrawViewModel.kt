package uk.lobsterdoodle.namepicker.ui.draw

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.lobsterdoodle.namepicker.shared.draw.DrawState
import uk.lobsterdoodle.namepicker.shared.draw.HatDraw
import uk.lobsterdoodle.namepicker.shared.draw.MIN_NAMES_TO_DRAW
import uk.lobsterdoodle.namepicker.shared.draw.SelectAllBox
import uk.lobsterdoodle.namepicker.shared.draw.clampCount
import uk.lobsterdoodle.namepicker.shared.draw.inHat
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository
import uk.lobsterdoodle.namepicker.shared.selection.sortNames
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup
import uk.lobsterdoodle.namepicker.shared.usecase.NameEditor
import uk.lobsterdoodle.namepicker.shared.usecase.SortOrder
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.status

/** A chip's look (spec "Chip states"): three visuals for the four selected/drawn combinations. */
enum class ChipVisual { IN, OUT, DRAWN }

data class ChipUi(val id: Long, val text: String, val visual: ChipVisual)

/** One drawn name, as it was when drawn: a later rename or delete never changes a result. */
data class RevealName(val id: Long, val text: String)

/** A stored result: the draw's id and its names. */
private data class DrawResult(val drawId: Long, val names: List<RevealName>)

data class RevealUi(
    /** One per draw, so a "Draw more" that happens to repeat the same names still plays and announces. */
    val drawId: Long,
    val names: List<RevealName>,
    /** "Draw n more": the chosen count, capped at what is left in the hat. */
    val more: Int,
    /** Nothing is left in the hat: "Everyone's been drawn" replaces "Draw n more". */
    val noneLeft: Boolean,
    /** The reveal's motion has run; rotation or restore shows the end layout without replaying. */
    val played: Boolean,
)

/**
 * A reset that can still be undone (spec "Draw from remaining": "Drawn names reset" with Undo).
 * [token] is new for every reset, so an older snackbar never undoes a newer one.
 */
data class PendingReset(val token: Long, val ids: List<Long>)

data class DrawUiState(
    /** False until the names and the sort order are read, so the empty state never flashes. */
    val loaded: Boolean = false,
    val sort: NameSortType = NameSortType.NONE,
    /** In the current sort order, keyed by name id (never by position). */
    val chips: List<ChipUi> = emptyList(),
    val draw: DrawState = DrawState.of(emptyList()),
    /** How many the next draw picks: 1..in the hat. */
    val count: Int = 1,
    val reveal: RevealUi? = null,
    /** Set from a reset until its snackbar is dismissed or Undo is tapped. */
    val pendingReset: PendingReset? = null,
)

/**
 * The Draw screen (spec "Draw", "Reveal"; NAM-10). Composes the `:shared` rules; the UI only renders
 * [uiState].
 *
 * - **Writes and draws are serialised** by one [Mutex]. Each re-reads the names inside it, so a
 *   name toggled off just before Draw is never drawn, and two quick taps on one chip both count.
 * - **The draw comes first:** the result is computed and stored in one step, then the reveal
 *   animates it. The animation never changes the outcome.
 * - **Saved state** holds the count, the result and whether the reveal has played, in
 *   platform-parcelable types, so all three survive rotation and process death (AC9).
 * - **Draw from remaining** (NAM-12): [HatDraw] marks the picks drawn before the result is stored, so a
 *   result is never shown that wasn't saved. Reset keeps an undo snapshot in saved state; Put back
 *   returns one drawn name to the hat.
 */
@HiltViewModel(assistedFactory = DrawViewModel.Factory::class)
class DrawViewModel @AssistedInject constructor(
    @Assisted private val listId: Long,
    groups: GroupRepository,
    private val activeGroup: ActiveGroup,
    private val names: NameRepository,
    private val editor: NameEditor,
    private val sortOrder: SortOrder,
    private val hat: HatDraw,
    private val saved: SavedStateHandle,
) : ViewModel() {

    val status: StateFlow<ListStatus> =
        groups.status(listId).stateIn(viewModelScope, SharingStarted.Eagerly, ListStatus.Loading)

    /** Null until first read. */
    private val stored: StateFlow<List<Name>?> =
        names.observeNames(listId).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val sort: StateFlow<NameSortType?> =
        sortOrder.observe(listId).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val count = MutableStateFlow(saved.get<Int>(KEY_COUNT) ?: 1)
    private val result = MutableStateFlow(restoreResult())
    private val played = MutableStateFlow(saved.get<Boolean>(KEY_PLAYED) ?: false)
    private val pendingReset = MutableStateFlow(restorePendingReset())

    private val writes = Mutex()
    private var drawing = false
    private var opened = false

    val uiState: StateFlow<DrawUiState> =
        combine(stored, sort, count, result, played) { names, sort, count, result, played ->
            if (names == null || sort == null) return@combine DrawUiState()
            val draw = DrawState.of(names)
            val n = clampCount(count, draw.inHat)
            DrawUiState(
                loaded = true,
                sort = sort,
                chips = sortNames(names, sort).map { ChipUi(it.id, it.text, it.visual()) },
                draw = draw,
                count = n,
                reveal = result?.let {
                    RevealUi(it.drawId, it.names, more = if (draw.inHat > 0) n else 0, noneLeft = draw.inHat == 0, played = played)
                },
            )
        }.combine(pendingReset) { ui, reset -> if (ui.loaded) ui.copy(pendingReset = reset) else ui }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DrawUiState())

    init {
        // Clamp the stored count whenever the pool shrinks below it (spec "Draw bar"), only once the
        // names have loaded, so a restored count is never wiped by a not-yet-read list.
        viewModelScope.launch {
            stored.filterNotNull().collect { names ->
                val clamped = clampCount(count.value, names.count { it.inHat })
                if (clamped != count.value) setCount(clamped)
            }
        }
    }

    /**
     * Opening Draw makes the list active (spec "Launch"). The entry calls this while it is started,
     * which a popped entry never is again, so a list the user has just left (or replaced in
     * list-detail) can't be stored after the fact. It does nothing until the list is known to
     * exist, so a stale id at launch is never stored again; the entry calls again once it is.
     */
    fun onShown() {
        if (opened || status.value !is ListStatus.Present) return
        opened = true
        viewModelScope.launch { activeGroup.open(listId) }
    }

    /** Puts a name in or out of the hat. Drawn names aren't toggled: they are put back ([putBack]). */
    fun toggle(id: Long) {
        viewModelScope.launch {
            writes.withLock {
                val name = current().firstOrNull { it.id == id } ?: return@withLock
                if (!name.drawn) editor.setSelected(id, !name.selected)
            }
        }
    }

    /** The select-all box: clears all when it is All, otherwise selects all. Never touches drawn. */
    fun toggleAll() {
        viewModelScope.launch {
            writes.withLock {
                when (DrawState.of(current()).box) {
                    SelectAllBox.ALL -> editor.clearAll(listId)
                    SelectAllBox.SOME, SelectAllBox.NONE -> editor.selectAll(listId)
                    SelectAllBox.DISABLED -> Unit
                }
            }
        }
    }

    /** "Put back in the hat" on a drawn chip: selected and no longer drawn. Ignored if it isn't drawn. */
    fun putBack(id: Long) {
        viewModelScope.launch {
            writes.withLock {
                val name = current().firstOrNull { it.id == id } ?: return@withLock
                if (name.drawn) hat.putBack(id)
            }
        }
    }

    /** Reset: clears drawn for the list, never selection, and offers Undo. With nothing drawn, nothing happens. */
    fun reset() {
        viewModelScope.launch {
            writes.withLock {
                val ids = hat.reset(listId)
                if (ids.isNotEmpty()) {
                    val token = (saved.get<Long>(KEY_RESET_TOKEN) ?: 0L) + 1
                    saved[KEY_RESET_TOKEN] = token
                    setPendingReset(PendingReset(token, ids))
                }
            }
        }
    }

    /** Undo on the reset snackbar: the names drawn before that reset are drawn again. */
    fun undoReset(token: Long) {
        viewModelScope.launch {
            writes.withLock {
                val pending = pendingReset.value?.takeIf { it.token == token } ?: return@withLock
                setPendingReset(null)
                hat.undoReset(pending.ids)
            }
        }
    }

    /** The reset snackbar timed out or was dismissed: the reset stands. */
    fun dropReset(token: Long) {
        if (pendingReset.value?.token == token) setPendingReset(null)
    }

    fun cycleSort() {
        viewModelScope.launch { sortOrder.cycle(listId) }
    }

    fun increment() = step(+1)

    fun decrement() = step(-1)

    /** Draws the chosen count from the hat and shows the result. Ignored while a result shows. */
    fun draw() {
        if (result.value != null) return
        drawInto()
    }

    /** "Draw n more" on the reveal: a new draw replaces the result and plays again. */
    fun drawMore() {
        if (result.value == null) return
        drawInto()
    }

    /** Done, back or Escape on the reveal. */
    fun dismiss() = setResult(null)

    /** The reveal's motion finished (or was skipped): don't replay it after rotation or restore. */
    fun onRevealPlayed() {
        played.value = true
        saved[KEY_PLAYED] = true
    }

    private fun drawInto() {
        if (drawing) return
        drawing = true
        // The result this request was made against; a Done that lands first cancels it.
        val against = result.value?.drawId
        viewModelScope.launch {
            try {
                writes.withLock {
                    if (result.value?.drawId != against) return@withLock
                    val names = current()
                    val inHat = names.count { it.inHat }
                    if (inHat < MIN_NAMES_TO_DRAW) return@withLock
                    // Marked drawn before the result is stored: a result that wasn't saved is never shown.
                    val picked = try {
                        hat.draw(listId, clampCount(count.value, inHat))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // The write failed (e.g. the list was deleted underneath): no result, no crash.
                        return@withLock
                    }
                    if (picked.isEmpty()) return@withLock
                    setResult(picked.map { RevealName(it.id, it.text) })
                }
            } finally {
                drawing = false
            }
        }
    }

    private fun step(delta: Int) {
        val inHat = uiState.value.draw.inHat
        setCount(clampCount(uiState.value.count + delta, inHat))
    }

    /** The names as stored now, after any write this ViewModel has made. */
    private suspend fun current(): List<Name> = names.observeNames(listId).first()

    private fun setCount(value: Int) {
        count.value = value
        saved[KEY_COUNT] = value
    }

    private fun setResult(value: List<RevealName>?) {
        val next = value?.let {
            val id = (saved.get<Long>(KEY_DRAW_ID) ?: 0L) + 1
            saved[KEY_DRAW_ID] = id
            DrawResult(id, it)
        }
        result.value = next
        played.value = false
        saved[KEY_RESULT_IDS] = value?.map { it.id }?.toLongArray()
        saved[KEY_RESULT_TEXTS] = value?.mapTo(ArrayList()) { it.text }
        saved[KEY_PLAYED] = false
    }

    private fun setPendingReset(value: PendingReset?) {
        pendingReset.value = value
        saved[KEY_RESET_IDS] = value?.ids?.toLongArray()
    }

    private fun restorePendingReset(): PendingReset? {
        val ids = saved.get<LongArray>(KEY_RESET_IDS) ?: return null
        return PendingReset(saved.get<Long>(KEY_RESET_TOKEN) ?: 0L, ids.toList())
    }

    private fun restoreResult(): DrawResult? {
        val ids = saved.get<LongArray>(KEY_RESULT_IDS) ?: return null
        val texts = saved.get<ArrayList<String>>(KEY_RESULT_TEXTS) ?: return null
        return DrawResult(saved.get<Long>(KEY_DRAW_ID) ?: 0L, ids.zip(texts) { id, text -> RevealName(id, text) })
    }

    private fun Name.visual() = when {
        drawn -> ChipVisual.DRAWN
        selected -> ChipVisual.IN
        else -> ChipVisual.OUT
    }

    @AssistedFactory
    interface Factory {
        fun create(listId: Long): DrawViewModel
    }

    companion object {
        const val KEY_COUNT = "count"
        const val KEY_RESULT_IDS = "resultIds"
        const val KEY_RESULT_TEXTS = "resultTexts"
        const val KEY_PLAYED = "revealPlayed"
        const val KEY_DRAW_ID = "drawId"
        const val KEY_RESET_TOKEN = "resetToken"
        const val KEY_RESET_IDS = "resetIds"
    }
}
