package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.Flow
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** Each group's name order, which the sort action cycles NONE → ASC → DESC → ASC. */
class SortOrder(private val prefs: PreferencesRepository) {

    fun observe(groupId: Long): Flow<NameSortType> = prefs.sortType(groupId)

    /** Moves the group to its next order, stores it and returns it. */
    suspend fun cycle(groupId: Long): NameSortType = prefs.updateSortType(groupId) { it.next() }
}
