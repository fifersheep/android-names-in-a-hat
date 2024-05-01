package uk.lobsterdoodle.namepicker.data.legacy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "column_group_id") val id: Long,
    @ColumnInfo(name = "column_group_name") val name: String
)
