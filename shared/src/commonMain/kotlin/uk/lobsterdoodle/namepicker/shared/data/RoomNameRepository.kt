package uk.lobsterdoodle.namepicker.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

internal class RoomNameRepository(database: NamesDatabase) : NameRepository {
    private val dao = database.nameDao()

    override fun observeNames(groupId: Long): Flow<List<Name>> =
        dao.observeByGroup(groupId).map { rows -> rows.map { it.toModel() } }

    override suspend fun add(groupId: Long, text: String, selected: Boolean): Long =
        dao.insert(NameEntity(groupId = groupId, text = text, selected = selected, drawn = false))

    override suspend fun rename(id: Long, text: String) = dao.rename(id, text)

    override suspend fun delete(id: Long): Name? = dao.delete(id)?.toModel()

    override suspend fun setSelected(id: Long, selected: Boolean) = dao.setSelected(id, selected)

    override suspend fun setAllSelected(groupId: Long, selected: Boolean) = dao.setAllSelected(groupId, selected)

    override suspend fun markDrawn(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        dao.markDrawn(ids.toList())
    }

    override suspend fun clearDrawn(groupId: Long) = dao.clearDrawn(groupId)

    private fun NameEntity.toModel() = Name(id, groupId, text, selected, drawn)
}
