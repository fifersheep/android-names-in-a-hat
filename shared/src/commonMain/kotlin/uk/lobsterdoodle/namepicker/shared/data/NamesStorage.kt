package uk.lobsterdoodle.namepicker.shared.data

import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

/**
 * The app's group and name storage. Room stays an implementation detail of `:shared`: callers
 * see only the repository interfaces. On Android, create one per process with
 * `NamesStorage(context)` and keep it for the app's lifetime (a singleton in the DI graph).
 */
class NamesStorage internal constructor(private val database: NamesDatabase) : AutoCloseable {
    val groups: GroupRepository = RoomGroupRepository(database)
    val names: NameRepository = RoomNameRepository(database)

    override fun close() = database.close()
}
