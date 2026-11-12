package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A row of `name_groups` (schema v3). `autoGenerate` gives AUTOINCREMENT, as the legacy
 * `table_groups` had, so ids of deleted groups are never handed out again.
 */
@Entity(tableName = "name_groups")
internal data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
)

/** A group with its name count, read by [GroupDao.observeSummaries]. */
internal data class GroupSummaryRow(
    val id: Long,
    val label: String,
    val nameCount: Int,
)
