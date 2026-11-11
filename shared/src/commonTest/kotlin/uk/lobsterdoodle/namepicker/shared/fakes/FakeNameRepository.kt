package uk.lobsterdoodle.namepicker.shared.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

/** In-memory [NameRepository] that records every write as a readable string in [calls]. */
class FakeNameRepository(initial: List<Name> = emptyList()) : NameRepository {
    val names = MutableStateFlow(initial)
    val calls = mutableListOf<String>()
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeNames(groupId: Long): Flow<List<Name>> =
        names.map { all -> all.filter { it.groupId == groupId } }

    override suspend fun add(groupId: Long, text: String, selected: Boolean): Long {
        calls += "add($groupId, $text, $selected)"
        val id = nextId++
        names.value = names.value + Name(id, groupId, text, selected)
        return id
    }

    override suspend fun rename(id: Long, text: String) {
        calls += "rename($id, $text)"
        update({ it.id == id }) { it.copy(text = text) }
    }

    override suspend fun delete(id: Long): Name? {
        calls += "delete($id)"
        val existing = names.value.firstOrNull { it.id == id } ?: return null
        names.value = names.value - existing
        return existing
    }

    override suspend fun setSelected(id: Long, selected: Boolean) {
        calls += "setSelected($id, $selected)"
        update({ it.id == id }) { it.copy(selected = selected) }
    }

    override suspend fun setAllSelected(groupId: Long, selected: Boolean) {
        calls += "setAllSelected($groupId, $selected)"
        update({ it.groupId == groupId }) { it.copy(selected = selected) }
    }

    override suspend fun markDrawn(ids: Collection<Long>) {
        calls += "markDrawn(${ids.sorted()})"
        update({ it.id in ids }) { it.copy(drawn = true) }
    }

    override suspend fun clearDrawn(groupId: Long) {
        calls += "clearDrawn($groupId)"
        update({ it.groupId == groupId }) { it.copy(drawn = false) }
    }

    private fun update(where: (Name) -> Boolean, change: (Name) -> Name) {
        names.value = names.value.map { if (where(it)) change(it) else it }
    }
}
