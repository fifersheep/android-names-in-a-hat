package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * A row of `names` (schema v3). Deleting a group deletes its names (CASCADE).
 * `selected` and `drawn` are independent flags.
 */
@Entity(
    tableName = "names",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("group_id")],
)
internal data class NameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "group_id") val groupId: Long,
    val text: String,
    @ColumnInfo(defaultValue = "0") val selected: Boolean,
    @ColumnInfo(defaultValue = "0") val drawn: Boolean = false,
)
