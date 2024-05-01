package uk.lobsterdoodle.namepicker.data.legacy

import androidx.room.*

/**
 * STRUCTURE
 *
 * table_names
 *  column_name_id : INTEGER
 *  column_name_name : TEXT, NOT NULL
 *  column_name_toggled : NUMERIC
 *  column_group_id : BLOB
 */

@Dao
interface NamesDao {
    @Query("SELECT * FROM ${Table.NAMES} WHERE ${Column.GROUP_ID} = :groupId")
    fun getNames(groupId: Long): List<NameEntity>

    @Insert
    fun insertName(name: NameEntity): Long

    @Delete
    fun deleteName(name: NameEntity)

    @Update
    fun updateName(name: NameEntity)

    @Update
    fun updateNames(vararg name: NameEntity)
}