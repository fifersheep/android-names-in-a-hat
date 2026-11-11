package uk.lobsterdoodle.namepicker.shared.repository

import kotlinx.coroutines.flow.Flow
import uk.lobsterdoodle.namepicker.shared.model.Name

/**
 * Stores the names in each group. Implemented by the Room database (NAM-5). Writes must not
 * run on the main thread. Text arrives already validated and trimmed; implementations store
 * it as given.
 *
 * The `selected` and `drawn` flags are independent: a write to one must never change the other.
 */
interface NameRepository {
    /** A group's names in insertion order (the order 2.7.0 showed them unsorted). */
    fun observeNames(groupId: Long): Flow<List<Name>>

    /** Adds a name, not drawn, and returns its id. */
    suspend fun add(groupId: Long, text: String, selected: Boolean): Long

    suspend fun rename(id: Long, text: String)

    /** Deletes one name. Returns the deleted name, or null if there was none. */
    suspend fun delete(id: Long): Name?

    suspend fun setSelected(id: Long, selected: Boolean)

    /** Sets `selected` on every name in the group in one write. Must not change `drawn`. */
    suspend fun setAllSelected(groupId: Long, selected: Boolean)

    /** Marks the given names drawn. Must not change `selected`. */
    suspend fun markDrawn(ids: Collection<Long>)

    /** Clears `drawn` on every name in the group. Must not change `selected`. */
    suspend fun clearDrawn(groupId: Long)
}
