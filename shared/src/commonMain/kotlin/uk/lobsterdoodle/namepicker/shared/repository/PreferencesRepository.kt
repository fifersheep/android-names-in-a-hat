package uk.lobsterdoodle.namepicker.shared.repository

import kotlinx.coroutines.flow.Flow
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

/**
 * Small per-install settings. Implemented with DataStore (NAM-6), which migrates the 2.7.0
 * `name_list` SharedPreferences keys. Reads and writes must not block the main thread.
 */
interface PreferencesRepository {
    /** The group to reopen on launch, or null when none is set (absence means none, as in 2.7.0). */
    val activeGroupId: Flow<Long?>

    suspend fun setActiveGroup(id: Long)

    suspend fun clearActiveGroup()

    /**
     * Atomically clears the active group only if it is [id]; used when that group is deleted.
     * An active group set to a different id in the meantime must be kept.
     */
    suspend fun clearActiveGroupIf(id: Long)

    /** The stored draw-screen column count, or 2 when nothing is stored. Not range-checked here. */
    val gridColumns: Flow<Int>

    suspend fun setGridColumns(count: Int)

    /** A group's name order; [NameSortType.NONE] when nothing, or an unrecognised value, is stored. */
    fun sortType(groupId: Long): Flow<NameSortType>

    /**
     * Atomically replaces a group's name order with [transform] applied to the current one,
     * and returns the new order. Two concurrent calls must not both see the same current value.
     */
    suspend fun updateSortType(groupId: Long, transform: (NameSortType) -> NameSortType): NameSortType

    /** Forgets a group's name order; called when the group is deleted. */
    suspend fun removeSortType(groupId: Long)
}
