package uk.lobsterdoodle.namepicker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository

/** In-memory [GroupRepository] for `:app` tests. */
class FakeGroups(vararg initial: Group) : GroupRepository {
    val groups = MutableStateFlow(initial.toList())

    /** Name and drawn counts per group id, read on each emission of [observeGroups]; absent = 0. */
    val counts = mutableMapOf<Long, Pair<Int, Int>>()
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeGroups(): Flow<List<GroupSummary>> =
        groups.map { all ->
            all.reversed().map {
                val (names, drawn) = counts[it.id] ?: (0 to 0)
                GroupSummary(it, nameCount = names, drawnCount = drawn)
            }
        }

    override fun observeGroup(id: Long): Flow<Group?> = groups.map { all -> all.find { it.id == id } }

    override suspend fun create(label: String): Long {
        val id = nextId++
        groups.value += Group(id, label)
        return id
    }

    override suspend fun rename(id: Long, label: String) {
        groups.value = groups.value.map { if (it.id == id) it.copy(label = label) else it }
    }

    override suspend fun delete(id: Long): Group? {
        val found = groups.value.find { it.id == id } ?: return null
        groups.value -= found
        return found
    }
}
