package uk.lobsterdoodle.namepicker.data.legacy

import androidx.room.*

/**
 * STRUCTURE
 *
 * table_groups
 *  column_group_id : INTEGER
 *  column_group_name : TEXT, NOT NULL
 */

@Dao
interface GroupsDao {
    @Query("SELECT * FROM ${Table.GROUPS}")
    fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM ${Table.GROUPS} WHERE ${Column.GROUP_ID} = :id LIMIT 1")
    fun getGroup(id: Long): GroupEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: GroupEntity): Long

    @Update
    fun updateGroup(group: GroupEntity)

    @Delete
    fun deleteGroup(group: GroupEntity)
}