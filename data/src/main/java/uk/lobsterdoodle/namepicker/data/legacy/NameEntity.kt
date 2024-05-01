package uk.lobsterdoodle.namepicker.data.legacy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NameEntity(
    @PrimaryKey @ColumnInfo(name = "column_name_ID") val id: Long,
    @ColumnInfo(name = "column_name_name") val name: String,
    @ColumnInfo(name = "column_name_toggled", defaultValue = "true") val toggled: Boolean
)
