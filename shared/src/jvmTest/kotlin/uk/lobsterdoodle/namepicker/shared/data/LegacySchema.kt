package uk.lobsterdoodle.namepicker.shared.data

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.io.File
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.Name

/** Every Android-framework database has this table; Room must leave it alone. */
private val ANDROID_METADATA = listOf(
    "CREATE TABLE android_metadata (locale TEXT)",
    "INSERT INTO android_metadata VALUES ('en_GB')",
)

/** v1 DDL, verbatim from `git show 200e6ef:app/src/main/java/uk/lobsterdoodle/namepicker/core/ClassroomDbHelper.java:55-63`. */
private val V1_DDL = listOf(
    "create table table_classroom_names(column_classroom_ID integer primary key autoincrement, " +
        "column_classroom_name text not null);",
    "create table table_pupil_names(column_pupil_ID integer primary key autoincrement, " +
        "column_pupil_name text not null, column_classroom_ID references " +
        "table_classroom_names(column_classroom_ID));",
)

/** v2 DDL, verbatim from `master:app/src/main/kotlin/uk/lobsterdoodle/namepicker/api/ClassroomDbHelper.kt:200-210`. */
private val V2_DDL = listOf(
    "create table table_groups(column_group_id integer primary key autoincrement, " +
        "column_group_name text not null);",
    "create table table_names(column_name_ID integer primary key autoincrement, " +
        "column_name_name text not null, column_name_toggled boolean default false, " +
        "column_group_id references table_groups(column_group_id));",
)

/**
 * A database as an older release left it, plus what v3 must contain afterwards.
 *
 * [groupSeq] and [nameSeq] are the legacy AUTOINCREMENT high-water marks. Each fixture deletes
 * its highest-id rows, so these exceed the largest surviving id: new rows must still get ids
 * above them.
 */
internal class LegacyFixture(
    val description: String,
    val version: Int,
    private val statements: List<String>,
    val expectedGroups: List<Group>,
    val expectedNames: Set<Name>,
    val groupSeq: Long,
    val nameSeq: Long,
) {
    fun writeTo(file: File) {
        val connection = BundledSQLiteDriver().open(file.path)
        try {
            (ANDROID_METADATA + statements).forEach(connection::execSQL)
            connection.execSQL("PRAGMA user_version = $version")
        } finally {
            connection.close()
        }
    }

    override fun toString() = description
}

private fun v2Group(id: Long, label: String) =
    "INSERT INTO table_groups(column_group_id, column_group_name) VALUES ($id, '$label')"

private fun v2Name(id: Long, text: String, toggled: String, groupId: String) =
    "INSERT INTO table_names(column_name_ID, column_name_name, column_name_toggled, column_group_id) " +
        "VALUES ($id, '$text', $toggled, $groupId)"

/** A typical 2.7.0 database: several groups, mixed selection, a deleted group and name. */
internal val CLEAN_V2 = LegacyFixture(
    description = "clean v2",
    version = 2,
    statements = V2_DDL + listOf(
        v2Group(1, "Class 1A"),
        v2Group(2, "Class 2B"),
        v2Group(3, "Year 3"),
        v2Group(4, "Deleted group"),
        v2Name(1, "Ann", "1", "1"),
        v2Name(2, "Bob", "0", "1"),
        v2Name(3, "Cara", "1", "2"),
        v2Name(4, "Dev", "0", "3"),
        v2Name(5, "Eve", "1", "3"),
        v2Name(6, "Finn", "1", "1"),
        v2Name(7, "O''Brien", "0", "2"),
        v2Name(8, "Deleted name", "1", "2"),
        // 2.7.0's removeGroup and removeName: the sequences stay at 4 and 8.
        "DELETE FROM table_names WHERE column_name_ID = 8",
        "DELETE FROM table_groups WHERE column_group_id = 4",
    ),
    expectedGroups = listOf(Group(1, "Class 1A"), Group(2, "Class 2B"), Group(3, "Year 3")),
    expectedNames = setOf(
        Name(1, 1, "Ann", selected = true),
        Name(2, 1, "Bob", selected = false),
        Name(3, 2, "Cara", selected = true),
        Name(4, 3, "Dev", selected = false),
        Name(5, 3, "Eve", selected = true),
        Name(6, 1, "Finn", selected = true),
        Name(7, 2, "O'Brien", selected = false),
    ),
    groupSeq = 4,
    nameSeq = 8,
)

/**
 * A 2.7.0 database with every awkward value the legacy code could leave:
 * NULL, text 'false' / 'true' and defaulted toggles, an orphan, a NULL group id,
 * and a group id stored as text.
 */
internal val DIRTY_V2 = LegacyFixture(
    description = "dirty v2",
    version = 2,
    statements = V2_DDL + listOf(
        v2Group(1, "G"),
        v2Group(2, "Empty group"),
        v2Name(1, "Null toggle", "NULL", "1"),
        v2Name(2, "False text", "'false'", "1"),
        v2Name(3, "One", "1", "1"),
        v2Name(4, "Orphan", "1", "99"),
        v2Name(5, "No group", "1", "NULL"),
        // Copied from v1 by 2.7.0's onUpgrade, which never set the toggle: the column default.
        "INSERT INTO table_names(column_name_ID, column_name_name, column_group_id) VALUES (6, 'Defaulted', 1)",
        v2Name(7, "True text", "'true'", "1"),
        v2Name(8, "Text group id", "1", "'1'"),
    ),
    expectedGroups = listOf(Group(1, "G"), Group(2, "Empty group")),
    expectedNames = setOf(
        // NULL and any non-numeric text read as 0 in 2.7.0 (cursor.getInt(...) > 0), so not selected.
        Name(1, 1, "Null toggle", selected = false),
        Name(2, 1, "False text", selected = false),
        Name(3, 1, "One", selected = true),
        Name(6, 1, "Defaulted", selected = false),
        Name(7, 1, "True text", selected = false),
        Name(8, 1, "Text group id", selected = true),
    ),
    groupSeq = 2,
    nameSeq = 8,
)

private fun v1Classroom(id: Long, name: String) =
    "INSERT INTO table_classroom_names(column_classroom_ID, column_classroom_name) VALUES ($id, '$name')"

private fun v1Pupil(id: Long, name: String, classroomId: String) =
    "INSERT INTO table_pupil_names(column_pupil_ID, column_pupil_name, column_classroom_ID) " +
        "VALUES ($id, '$name', $classroomId)"

/**
 * A pre-2017 database. It includes the orphans the v1 code really produced: `addPupil` stored
 * `-1` when `getClassroomId` found no classroom (200e6ef ClassroomDbHelper.java:171,178,277-278).
 */
internal val V1 = LegacyFixture(
    description = "v1",
    version = 1,
    statements = V1_DDL + listOf(
        v1Classroom(1, "Room 1"),
        v1Classroom(2, "Room 2"),
        v1Classroom(3, "Deleted room"),
        v1Pupil(1, "Ann", "1"),
        v1Pupil(2, "Bob", "2"),
        v1Pupil(3, "Cara", "1"),
        v1Pupil(4, "Minus one", "-1"),
        v1Pupil(5, "No classroom", "NULL"),
        v1Pupil(6, "Deleted pupil", "2"),
        "DELETE FROM table_pupil_names WHERE column_pupil_ID = 6",
        "DELETE FROM table_classroom_names WHERE column_classroom_ID = 3",
    ),
    expectedGroups = listOf(Group(1, "Room 1"), Group(2, "Room 2")),
    // v1 had no selection; the legacy upgrade left the column at its default, shown as not selected.
    expectedNames = setOf(
        Name(1, 1, "Ann", selected = false),
        Name(2, 2, "Bob", selected = false),
        Name(3, 1, "Cara", selected = false),
    ),
    groupSeq = 3,
    nameSeq = 6,
)

internal val ALL_FIXTURES = listOf(CLEAN_V2, DIRTY_V2, V1)
