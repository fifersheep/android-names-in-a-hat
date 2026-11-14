package uk.lobsterdoodle.namepicker.ui.placeholder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor
import uk.lobsterdoodle.namepicker.shared.usecase.WriteResult

// Placeholder ViewModels for the shell (NAM-7). The screen tickets (NAM-8/9/10) grow these into
// the real screens' ViewModels; the list-status handling and the active-list rule stay.

/** Whether the list a destination shows still exists. */
sealed interface ListStatus {
    data object Loading : ListStatus

    /** Edit list before the list is created. */
    data object Unsaved : ListStatus

    data class Present(val label: String) : ListStatus

    /** Deleted, or a stale id: the shell drops the destination. */
    data object Gone : ListStatus
}

private fun GroupRepository.status(id: Long) =
    observeGroup(id).map { group -> group?.let { ListStatus.Present(it.label) } ?: ListStatus.Gone }

@HiltViewModel
class ListsViewModel @Inject constructor(
    groups: GroupRepository,
    private val editor: GroupEditor,
) : ViewModel() {

    val lists: StateFlow<List<GroupSummary>> =
        groups.observeGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Immediate delete, to exercise the shell. NAM-8 replaces it with the delayed delete (D12). */
    fun delete(id: Long) {
        viewModelScope.launch { editor.delete(id) }
    }
}

@HiltViewModel(assistedFactory = DrawViewModel.Factory::class)
class DrawViewModel @AssistedInject constructor(
    @Assisted private val listId: Long,
    groups: GroupRepository,
    private val activeGroup: ActiveGroup,
) : ViewModel() {

    val status: StateFlow<ListStatus> =
        groups.status(listId).stateIn(viewModelScope, SharingStarted.Eagerly, ListStatus.Loading)

    private var opened = false

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

    @AssistedFactory
    interface Factory {
        fun create(listId: Long): DrawViewModel
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = EditListViewModel.Factory::class)
class EditListViewModel @AssistedInject constructor(
    @Assisted listId: Long?,
    groups: GroupRepository,
    private val editor: GroupEditor,
) : ViewModel() {

    /**
     * The list being edited. Starts from the key; set here when the list is created, because the
     * entry (and so this ViewModel) is kept when the shell swaps `EditList(null)` for `EditList(id)`.
     */
    private val id = MutableStateFlow(listId)
    private var creating = false

    val status: StateFlow<ListStatus> = id
        .flatMapLatest { id -> if (id == null) flowOf(ListStatus.Unsaved) else groups.status(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, if (listId == null) ListStatus.Unsaved else ListStatus.Loading)

    /** Creates the list once; a blank name creates nothing. Calls [onCreated] with the new id. */
    fun create(raw: String, onCreated: (Long) -> Unit) {
        if (id.value != null || creating) return
        creating = true
        viewModelScope.launch {
            when (val result = editor.create(raw)) {
                is WriteResult.Saved -> {
                    id.value = result.value
                    onCreated(result.value)
                }
                WriteResult.Blank -> Unit
            }
            creating = false
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(listId: Long?): EditListViewModel
    }
}
