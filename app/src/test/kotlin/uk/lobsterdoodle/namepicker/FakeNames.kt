package uk.lobsterdoodle.namepicker

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

/** In-memory [NameRepository] for `:app` tests. Every write is recorded in [calls]. */
class FakeNames(vararg initial: Name) : NameRepository {
    val names = MutableStateFlow(initial.toList())
    val calls = mutableListOf<String>()
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    /** Makes the next writes throw, as Room does when the list was deleted underneath (foreign key). */
    var failWrites = false

    /** While set, selection writes suspend until it completes: a slow database write (NAM-10 race tests). */
    var selectionGate: CompletableDeferred<Unit>? = null

    /** While set, [markDrawn] suspends until it completes: a slow draw write (NAM-12). */
    var drawGate: CompletableDeferred<Unit>? = null

    fun texts(groupId: Long) = names.value.filter { it.groupId == groupId }.map { it.text }

    override fun observeNames(groupId: Long): Flow<List<Name>> =
        names.map { all -> all.filter { it.groupId == groupId } }

    override suspend fun add(groupId: Long, text: String, selected: Boolean): Long {
        calls += "add($groupId, $text, $selected)"
        check(!failWrites) { "FOREIGN KEY constraint failed" }
        val id = nextId++
        names.value = names.value + Name(id, groupId, text, selected)
        return id
    }

    override suspend fun rename(id: Long, text: String) {
        calls += "rename($id, $text)"
        check(!failWrites) { "write failed" }
        update({ it.id == id }) { it.copy(text = text) }
    }

    override suspend fun delete(id: Long): Name? {
        calls += "delete($id)"
        val existing = names.value.firstOrNull { it.id == id } ?: return null
        names.value = names.value - existing
        return existing
    }

    override suspend fun setSelected(id: Long, selected: Boolean) {
        selectionGate?.await()
        update({ it.id == id }) { it.copy(selected = selected) }
    }

    override suspend fun setAllSelected(groupId: Long, selected: Boolean) {
        selectionGate?.await()
        update({ it.groupId == groupId }) { it.copy(selected = selected) }
    }

    override suspend fun markDrawn(ids: Collection<Long>) {
        calls += "markDrawn($ids)"
        drawGate?.await()
        check(!failWrites) { "write failed" }
        update({ it.id in ids }) { it.copy(drawn = true) }
    }

    override suspend fun clearDrawn(groupId: Long) {
        calls += "clearDrawn($groupId)"
        update({ it.groupId == groupId }) { it.copy(drawn = false) }
    }

    override suspend fun clearDrawn(ids: Collection<Long>) {
        calls += "clearDrawn($ids)"
        update({ it.id in ids }) { it.copy(drawn = false) }
    }

    private fun update(where: (Name) -> Boolean, change: (Name) -> Name) {
        names.value = names.value.map { if (where(it)) change(it) else it }
    }
}
