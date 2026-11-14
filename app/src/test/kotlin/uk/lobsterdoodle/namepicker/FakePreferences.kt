package uk.lobsterdoodle.namepicker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.io.IOException
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** In-memory [PreferencesRepository] for `:app` tests; records active-group writes in [calls]. */
class FakePreferences(activeGroupId: Long? = null, private val failReads: Boolean = false) : PreferencesRepository {
    val active = MutableStateFlow(activeGroupId)
    val calls = mutableListOf<String>()

    override val activeGroupId: Flow<Long?> =
        if (failReads) flow { throw IOException("settings unreadable") } else active

    override suspend fun setActiveGroup(id: Long) {
        calls += "setActiveGroup($id)"
        active.value = id
    }

    override suspend fun clearActiveGroup() {
        calls += "clearActiveGroup()"
        active.value = null
    }

    override suspend fun clearActiveGroupIf(id: Long) {
        calls += "clearActiveGroupIf($id)"
        if (active.value == id) active.value = null
    }

    override val gridColumns: Flow<Int> = flowOf(2)

    override suspend fun setGridColumns(count: Int) = Unit

    override fun sortType(groupId: Long): Flow<NameSortType> = flowOf(NameSortType.NONE)

    override suspend fun updateSortType(groupId: Long, transform: (NameSortType) -> NameSortType) =
        transform(NameSortType.NONE)

    override suspend fun removeSortType(groupId: Long) = Unit
}
