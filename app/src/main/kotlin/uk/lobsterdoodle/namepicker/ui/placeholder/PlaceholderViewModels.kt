package uk.lobsterdoodle.namepicker.ui.placeholder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.status

// The Draw placeholder's ViewModel (NAM-7). NAM-10 grows it into the Draw screen's ViewModel;
// the list-status handling and the active-list rule stay.

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
