package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GroupDao {
    /** Newest first, as 2.7.0 listed them (`ORDER BY column_group_id DESC`). */
    @Query(
        "SELECT g.id AS id, g.label AS label, COUNT(n.id) AS nameCount " +
            "FROM name_groups g LEFT JOIN names n ON n.group_id = g.id " +
            "GROUP BY g.id ORDER BY g.id DESC",
    )
    fun observeSummaries(): Flow<List<GroupSummaryRow>>

    @Query("SELECT * FROM name_groups WHERE id = :id")
    fun observe(id: Long): Flow<GroupEntity?>

    @Query("SELECT * FROM name_groups WHERE id = :id")
    suspend fun get(id: Long): GroupEntity?

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Query("UPDATE name_groups SET label = :label WHERE id = :id")
    suspend fun rename(id: Long, label: String)

    @Query("DELETE FROM name_groups WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Deletes the group (its names go by CASCADE) and returns it, or null if there was none. */
    @Transaction
    suspend fun delete(id: Long): GroupEntity? {
        val group = get(id) ?: return null
        deleteById(id)
        return group
    }
}
