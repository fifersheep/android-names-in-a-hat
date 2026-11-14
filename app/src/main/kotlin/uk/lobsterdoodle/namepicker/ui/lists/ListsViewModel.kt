package uk.lobsterdoodle.namepicker.ui.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor
import uk.lobsterdoodle.namepicker.ui.PendingDeletes

/** One card on the Lists screen. */
data class ListCard(val id: Long, val label: String, val nameCount: Int, val drawnCount: Int)

/** A list hidden by a delete that can still be undone. [reopen]: it was open in the detail pane. */
data class PendingDelete(val id: Long, val label: String, val reopen: Boolean)

data class ListsUiState(
    val lists: List<ListCard> = emptyList(),
    /** False until the first read, so the empty state doesn't flash before the lists arrive. */
    val loaded: Boolean = false,
    val pending: PendingDelete? = null,
)

/**
 * The Lists screen (spec "Lists"): every list, newest first, live, and the delayed delete (D12).
 *
 * A delete hides the list at once and is only committed later: when the snackbar times out
 * ([commit]), when the user leaves the screen or the app ([commitPending]), when another delete
 * replaces it, or when this ViewModel is cleared. Undo un-hides it; nothing is re-inserted, so the
 * names, ids, sort order and active list are all kept. The pending delete lives in saved state, so
 * rotation and process death don't lose it. Commits run on the process-lifetime [appScope] because
 * `viewModelScope` is already cancelled in [onCleared].
 */
@HiltViewModel
class ListsViewModel @Inject constructor(
    groups: GroupRepository,
    private val editor: GroupEditor,
    private val pendingDeletes: PendingDeletes,
    @param:AppScope private val appScope: CoroutineScope,
    private val saved: SavedStateHandle,
) : ViewModel() {

    private val pending = MutableStateFlow(restorePending())

    /** Deletes committed by this ViewModel. Ids are never reused (AUTOINCREMENT), so they stay hidden. */
    private val committed = MutableStateFlow(emptySet<Long>())

    init {
        pending.value?.let { pendingDeletes.add(it.id) }
    }

    val uiState: StateFlow<ListsUiState> =
        combine(groups.observeGroups(), pending, committed) { summaries, pending, committed ->
            ListsUiState(
                lists = summaries
                    .filter { it.group.id != pending?.id && it.group.id !in committed }
                    .map { ListCard(it.group.id, it.group.label, it.nameCount, it.drawnCount) },
                loaded = true,
                pending = pending,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListsUiState(pending = pending.value))

    /** Hides list [id] and starts its undo window, committing any earlier pending delete first. */
    fun delete(id: Long, label: String, wasOpen: Boolean) {
        commitPending()
        setPending(PendingDelete(id, label, wasOpen))
        pendingDeletes.add(id)
    }

    /** Undoes the delete of [id] and returns it, or null if [id] is no longer the pending delete. */
    fun undo(id: Long): PendingDelete? {
        val undone = pending.value?.takeIf { it.id == id } ?: return null
        setPending(null)
        pendingDeletes.remove(id)
        return undone
    }

    /** The undo window for [id] has closed. Does nothing if [id] is no longer the pending delete. */
    fun commit(id: Long) {
        if (pending.value?.id == id) commitPending()
    }

    /** Commits the pending delete, if there is one. */
    fun commitPending() {
        val doomed = pending.value ?: return
        committed.update { it + doomed.id }
        setPending(null)
        appScope.launch {
            try {
                editor.delete(doomed.id)
            } finally {
                pendingDeletes.remove(doomed.id)
            }
        }
    }

    override fun onCleared() = commitPending()

    private fun setPending(value: PendingDelete?) {
        pending.value = value
        saved[KEY_ID] = value?.id
        saved[KEY_LABEL] = value?.label
        saved[KEY_REOPEN] = value?.reopen
    }

    private fun restorePending(): PendingDelete? {
        val id = saved.get<Long>(KEY_ID) ?: return null
        return PendingDelete(id, saved.get<String>(KEY_LABEL).orEmpty(), saved.get<Boolean>(KEY_REOPEN) ?: false)
    }

    private companion object {
        const val KEY_ID = "pendingDeleteId"
        const val KEY_LABEL = "pendingDeleteLabel"
        const val KEY_REOPEN = "pendingDeleteReopen"
    }
}
