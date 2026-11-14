package uk.lobsterdoodle.namepicker.ui.editlist

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.di.AppScope
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository
import uk.lobsterdoodle.namepicker.shared.usecase.NameEditor
import uk.lobsterdoodle.namepicker.shared.usecase.validateName
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.PendingDeletes
import uk.lobsterdoodle.namepicker.ui.status

/** One row in Edit list's names section. */
data class NameRow(val id: Long, val text: String)

/** A name hidden by a delete that can still be undone. */
data class PendingNameDelete(val id: Long, val text: String)

data class EditNamesUiState(
    val names: List<NameRow> = emptyList(),
    /** False until the first read, so "0 names" doesn't flash before the names arrive. */
    val loaded: Boolean = false,
    val pending: PendingNameDelete? = null,
)

/**
 * Edit list's names section (spec "Edit list", D12–D15) for one existing list. The list name is
 * [EditListViewModel]'s; this ViewModel only exists once the list does.
 *
 * - **Add** validates here first ([validateName]: inner tabs and line breaks become spaces, the
 *   ends are trimmed, blank shows "Enter a name"), clears the field at once, then writes on the
 *   process-lifetime [appScope]. So two quick Sends can't insert twice, and Done straight after Add
 *   can't cancel the write.
 * - **Rename** is inline, one row at a time. Opening another row, or leaving, saves a valid changed
 *   draft; a blank one is dropped and the old name kept.
 * - **Delete** is the delayed delete: the row is hidden at once and committed when the snackbar
 *   ends ([commit]), on a second delete, on leaving the app ([commitPending]) or when this
 *   ViewModel is cleared. Undo un-hides it, so its id, position and drawn state are kept.
 *
 * Nothing is written on leaving while the list itself is pending a delete or gone (NAM-8's
 * [PendingDeletes] contract), so Undo of the list brings its names back unchanged. Typed add text
 * is never added on leaving: it is a new name, like a new list's unsaved name.
 *
 * Text, errors, the open rename and the pending delete are snapshot or flow state backed by
 * [SavedStateHandle], so they survive rotation and process death.
 */
@HiltViewModel(assistedFactory = EditNamesViewModel.Factory::class)
class EditNamesViewModel @AssistedInject constructor(
    @Assisted private val listId: Long,
    names: NameRepository,
    groups: GroupRepository,
    private val editor: NameEditor,
    private val pendingDeletes: PendingDeletes,
    @param:AppScope private val appScope: CoroutineScope,
    private val saved: SavedStateHandle,
) : ViewModel() {

    var addText: String by mutableStateOf(saved.get<String>(KEY_ADD_TEXT).orEmpty())
        private set

    /** The "Enter a name" error on the add field. */
    var addError: Boolean by mutableStateOf(saved.get<Boolean>(KEY_ADD_ERROR) ?: false)
        private set

    /** The name being renamed inline, or null. */
    var renamingId: Long? by mutableStateOf(saved.get<Long>(KEY_RENAME_ID))
        private set

    var renameText: String by mutableStateOf(saved.get<String>(KEY_RENAME_TEXT).orEmpty())
        private set

    /** The "Enter a name" error on the inline rename field. */
    var renameError: Boolean by mutableStateOf(saved.get<Boolean>(KEY_RENAME_ERROR) ?: false)
        private set

    /** The renamed name's stored text, so an unchanged save writes nothing. */
    private var renameOriginal: String = saved.get<String>(KEY_RENAME_ORIGINAL).orEmpty()

    private val pending = MutableStateFlow(restorePending())

    /** Deletes committed here. Ids are never reused (AUTOINCREMENT), so they stay hidden. */
    private val committed = MutableStateFlow(emptySet<Long>())

    private var listGone = false

    init {
        viewModelScope.launch {
            groups.status(listId).collect { listGone = it == ListStatus.Gone }
        }
    }

    val uiState: StateFlow<EditNamesUiState> =
        combine(names.observeNames(listId), pending, committed) { all, pending, committed ->
            EditNamesUiState(
                names = all.filter { it.id != pending?.id && it.id !in committed }.map { NameRow(it.id, it.text) },
                loaded = true,
                pending = pending,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditNamesUiState(pending = pending.value))

    // Add

    fun onAddTextChange(value: String) {
        storeAddText(value)
        storeAddError(false)
    }

    /**
     * Adds the typed name. Returns true if it was valid: the field is then already clear and the
     * write is under way. Blank shows the error, keeps the text and returns false.
     */
    fun add(): Boolean {
        val name = validateName(addText)
        if (name == null) {
            storeAddError(true)
            return false
        }
        storeAddText("")
        write("add") { editor.add(listId, name) }
        return true
    }

    // Rename

    /** Opens the inline rename on name [id], whose stored text is [text]. Closes any other first. */
    fun startRename(id: Long, text: String) {
        if (renamingId == id) return
        closeRename(save = true)
        renameOriginal = text
        saved[KEY_RENAME_ORIGINAL] = text
        storeRenameId(id)
        // A blank 2.7.0 name shows as "Unnamed" but is edited from empty.
        storeRenameText(if (text.isBlank()) "" else text)
        storeRenameError(false)
    }

    fun onRenameTextChange(value: String) {
        storeRenameText(value)
        storeRenameError(false)
    }

    /** ✓ or IME Done. Saves a valid name and closes; blank shows the error, keeps the old name and stays open. */
    fun saveRename(): Boolean {
        val id = renamingId ?: return false
        val name = validateName(renameText)
        if (name == null) {
            storeRenameError(true)
            return false
        }
        clearRename()
        if (name != renameOriginal) write("rename") { editor.rename(id, name) }
        return true
    }

    /** ✕: closes the rename without saving. */
    fun cancelRename() = clearRename()

    // Delayed delete

    /** Hides name [id] and starts its undo window, committing any earlier pending delete first. */
    fun delete(id: Long, text: String) {
        if (renamingId == id) clearRename()
        commitPending()
        setPending(PendingNameDelete(id, text))
    }

    /** Undoes the delete of [id] and returns it, or null if [id] is no longer the pending delete. */
    fun undo(id: Long): PendingNameDelete? {
        val undone = pending.value?.takeIf { it.id == id } ?: return null
        setPending(null)
        return undone
    }

    /** The undo window for [id] has closed. Does nothing if [id] is no longer the pending delete. */
    fun commit(id: Long) {
        if (pending.value?.id == id) commitPending()
    }

    /** Commits the pending delete, if there is one. Dropped, not written, if the list is being deleted. */
    fun commitPending() {
        val doomed = pending.value ?: return
        setPending(null)
        if (!listWritable()) return
        committed.update { it + doomed.id }
        write("delete") { editor.delete(doomed.id) }
    }

    /** Leaving the screen: commit the pending delete and a valid rename draft; never the add text. */
    override fun onCleared() {
        if (!listWritable()) return
        commitPending()
        closeRename(save = true)
    }

    // Internals

    private fun listWritable() = !listGone && listId !in pendingDeletes

    /** Closes the open rename, saving a valid changed draft if [save]; a blank draft is dropped. */
    private fun closeRename(save: Boolean) {
        val id = renamingId ?: return
        val name = validateName(renameText)
        clearRename()
        if (save && name != null && name != renameOriginal) write("rename") { editor.rename(id, name) }
    }

    private fun clearRename() {
        storeRenameId(null)
        storeRenameText("")
        storeRenameError(false)
    }

    /**
     * Runs a write on the app scope, so leaving the screen can't cancel it. A failure is logged, not
     * thrown: the list can be deleted underneath (its foreign key then rejects the write).
     */
    private fun write(what: String, block: suspend () -> Unit) {
        appScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Log.w(TAG, "Name $what failed for list $listId", e)
            }
        }
    }

    private fun setPending(value: PendingNameDelete?) {
        pending.value = value
        saved[KEY_PENDING_ID] = value?.id
        saved[KEY_PENDING_TEXT] = value?.text
    }

    private fun restorePending(): PendingNameDelete? {
        val id = saved.get<Long>(KEY_PENDING_ID) ?: return null
        return PendingNameDelete(id, saved.get<String>(KEY_PENDING_TEXT).orEmpty())
    }

    private fun storeAddText(value: String) {
        addText = value
        saved[KEY_ADD_TEXT] = value
    }

    private fun storeAddError(value: Boolean) {
        addError = value
        saved[KEY_ADD_ERROR] = value
    }

    private fun storeRenameId(value: Long?) {
        renamingId = value
        saved[KEY_RENAME_ID] = value
    }

    private fun storeRenameText(value: String) {
        renameText = value
        saved[KEY_RENAME_TEXT] = value
    }

    private fun storeRenameError(value: Boolean) {
        renameError = value
        saved[KEY_RENAME_ERROR] = value
    }

    @AssistedFactory
    interface Factory {
        fun create(listId: Long): EditNamesViewModel
    }

    private companion object {
        const val TAG = "EditNames"
        const val KEY_ADD_TEXT = "addText"
        const val KEY_ADD_ERROR = "addError"
        const val KEY_RENAME_ID = "renameId"
        const val KEY_RENAME_TEXT = "renameText"
        const val KEY_RENAME_ERROR = "renameError"
        const val KEY_RENAME_ORIGINAL = "renameOriginal"
        const val KEY_PENDING_ID = "pendingDeleteId"
        const val KEY_PENDING_TEXT = "pendingDeleteText"
    }
}
