package uk.lobsterdoodle.namepicker.shared.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository

/** In-memory [GroupRepository] that records every write as a readable string in [calls]. */
class FakeGroupRepository(initial: List<Group> = emptyList()) : GroupRepository {
    val groups = MutableStateFlow(initial)
    val calls = mutableListOf<String>()
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeGroups(): Flow<List<GroupSummary>> =
        groups.map { all -> all.sortedByDescending { it.id }.map { GroupSummary(it, nameCount = 0) } }

    override fun observeGroup(id: Long): Flow<Group?> = groups.map { all -> all.firstOrNull { it.id == id } }

    override suspend fun create(label: String): Long {
        calls += "create($label)"
        val id = nextId++
        groups.value = groups.value + Group(id, label)
        return id
    }

    override suspend fun rename(id: Long, label: String) {
        calls += "rename($id, $label)"
        groups.value = groups.value.map { if (it.id == id) it.copy(label = label) else it }
    }

    override suspend fun delete(id: Long): Group? {
        calls += "delete($id)"
        val existing = groups.value.firstOrNull { it.id == id } ?: return null
        groups.value = groups.value - existing
        return existing
    }
}
