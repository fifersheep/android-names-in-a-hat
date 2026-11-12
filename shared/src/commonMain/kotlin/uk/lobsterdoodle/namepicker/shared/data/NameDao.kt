package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface NameDao {
    /** Insertion order: ids are AUTOINCREMENT, so ascending id is the order names were added. */
    @Query("SELECT * FROM names WHERE group_id = :groupId ORDER BY id ASC")
    fun observeByGroup(groupId: Long): Flow<List<NameEntity>>

    @Query("SELECT * FROM names WHERE id = :id")
    suspend fun get(id: Long): NameEntity?

    @Insert
    suspend fun insert(name: NameEntity): Long

    @Query("UPDATE names SET text = :text WHERE id = :id")
    suspend fun rename(id: Long, text: String)

    @Query("DELETE FROM names WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Deletes one name and returns it, or null if there was none. */
    @Transaction
    suspend fun delete(id: Long): NameEntity? {
        val name = get(id) ?: return null
        deleteById(id)
        return name
    }

    @Query("UPDATE names SET selected = :selected WHERE id = :id")
    suspend fun setSelected(id: Long, selected: Boolean)

    @Query("UPDATE names SET selected = :selected WHERE group_id = :groupId")
    suspend fun setAllSelected(groupId: Long, selected: Boolean)

    @Query("UPDATE names SET drawn = 1 WHERE id IN (:ids)")
    suspend fun markDrawn(ids: List<Long>)

    @Query("UPDATE names SET drawn = 0 WHERE group_id = :groupId")
    suspend fun clearDrawn(groupId: Long)
}
