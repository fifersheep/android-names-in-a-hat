package uk.lobsterdoodle.namepicker.shared.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** In-memory [PreferencesRepository] that records every write as a readable string in [calls]. */
class FakePreferencesRepository(
    activeGroupId: Long? = null,
    gridColumns: Int = 2,
    sortTypes: Map<Long, NameSortType> = emptyMap(),
) : PreferencesRepository {
    val activeGroup = MutableStateFlow(activeGroupId)
    val columns = MutableStateFlow(gridColumns)
    val sorts = MutableStateFlow(sortTypes)
    val calls = mutableListOf<String>()

    override val activeGroupId: Flow<Long?> = activeGroup
    override val gridColumns: Flow<Int> = columns

    override suspend fun setActiveGroup(id: Long) {
        calls += "setActiveGroup($id)"
        activeGroup.value = id
    }

    override suspend fun clearActiveGroup() {
        calls += "clearActiveGroup()"
        activeGroup.value = null
    }

    override suspend fun setGridColumns(count: Int) {
        calls += "setGridColumns($count)"
        columns.value = count
    }

    override fun sortType(groupId: Long): Flow<NameSortType> = sorts.map { it[groupId] ?: NameSortType.NONE }

    override suspend fun updateSortType(groupId: Long, transform: (NameSortType) -> NameSortType): NameSortType {
        val updated = transform(sorts.value[groupId] ?: NameSortType.NONE)
        calls += "updateSortType($groupId) -> $updated"
        sorts.value = sorts.value + (groupId to updated)
        return updated
    }

    override suspend fun removeSortType(groupId: Long) {
        calls += "removeSortType($groupId)"
        sorts.value = sorts.value - groupId
    }
}
