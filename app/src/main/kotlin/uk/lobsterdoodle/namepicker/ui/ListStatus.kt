package uk.lobsterdoodle.namepicker.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository

/** Whether the list a destination shows still exists. */
sealed interface ListStatus {
    data object Loading : ListStatus

    /** Edit list before the list is created. */
    data object Unsaved : ListStatus

    data class Present(val label: String) : ListStatus

    /** Deleted, or a stale id: the shell drops the destination. */
    data object Gone : ListStatus
}

/** The status of list [id], following renames and deletes. */
fun GroupRepository.status(id: Long): Flow<ListStatus> =
    observeGroup(id).map { group -> group?.let { ListStatus.Present(it.label) } ?: ListStatus.Gone }
