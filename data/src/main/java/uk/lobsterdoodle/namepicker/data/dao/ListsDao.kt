package uk.lobsterdoodle.namepicker.data.dao

import androidx.room.Dao

/**
 * ORIGINAL STRUCTURE
 *
 * table_groups
 *  column_group_id : INTEGER
 *  column_group_name : TEXT, NOT NULL
 *
 * table_names
 *  column_name_id : INTEGER
 *  column_name_name : TEXT, NOT NULL
 *  column_name_toggled : NUMERIC
 *  column_group_id : BLOB
 */

@Dao
class ListsDao {
}