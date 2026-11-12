package uk.lobsterdoodle.namepicker.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository

internal class RoomGroupRepository(database: NamesDatabase) : GroupRepository {
    private val dao = database.groupDao()

    override fun observeGroups(): Flow<List<GroupSummary>> =
        dao.observeSummaries().map { rows -> rows.map { GroupSummary(Group(it.id, it.label), it.nameCount) } }

    override fun observeGroup(id: Long): Flow<Group?> = dao.observe(id).map { it?.toModel() }

    override suspend fun create(label: String): Long = dao.insert(GroupEntity(label = label))

    override suspend fun rename(id: Long, label: String) = dao.rename(id, label)

    override suspend fun delete(id: Long): Group? = dao.delete(id)?.toModel()

    private fun GroupEntity.toModel() = Group(id, label)
}
