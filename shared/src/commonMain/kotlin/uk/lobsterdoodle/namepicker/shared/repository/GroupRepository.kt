package uk.lobsterdoodle.namepicker.shared.repository

import kotlinx.coroutines.flow.Flow
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary

/**
 * Stores groups. Implemented by the Room database (NAM-5). Writes must not run on the main
 * thread. Labels arrive already validated and trimmed; implementations store them as given.
 */
interface GroupRepository {
    /** Every group with its name count, newest first (2.7.0 ordered by id descending). */
    fun observeGroups(): Flow<List<GroupSummary>>

    /** One group, or null once it no longer exists. */
    fun observeGroup(id: Long): Flow<Group?>

    /** Creates a group and returns its id. */
    suspend fun create(label: String): Long

    suspend fun rename(id: Long, label: String)

    /** Deletes the group **and all of its names**. Returns the deleted group, or null if there was none. */
    suspend fun delete(id: Long): Group?
}
