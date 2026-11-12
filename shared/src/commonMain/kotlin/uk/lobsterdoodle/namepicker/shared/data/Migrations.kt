package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/*
 * Migrations from the hand-written SQLiteOpenHelper schemas that shipped before Room.
 *
 * v1 (2014–2017): table_classroom_names / table_pupil_names.
 * v2 (2017–2.7.0): table_groups / table_names, written by ClassroomDbHelper on `master`.
 * v3 (3.0, Room): name_groups / names, with drawn state.
 *
 * These must never be removed or made destructive: Android backup can restore a v1 or v2
 * database into any later install. Room runs each migration inside one exclusive transaction,
 * so no BEGIN/COMMIT here.
 */

/**
 * Same result as 2.7.0's `onUpgrade` (master: ClassroomDbHelper.kt:21-34): create the v2 tables
 * with the legacy DDL, copy classrooms and pupils across, drop the v1 tables. Two deliberate
 * additions: the AUTOINCREMENT sequences are carried over, and the child table is dropped first.
 */
internal val MIGRATION_1_2 = Migration(1, 2) { connection ->
    // Legacy v2 DDL, verbatim (master: ClassroomDbHelper.kt:200-210).
    connection.execSQL(
        "create table table_groups(column_group_id integer primary key autoincrement, " +
            "column_group_name text not null);",
    )
    connection.execSQL(
        "create table table_names(column_name_ID integer primary key autoincrement, " +
            "column_name_name text not null, column_name_toggled boolean default false, " +
            "column_group_id references table_groups(column_group_id));",
    )
    connection.execSQL(
        "INSERT INTO table_groups(column_group_id, column_group_name) " +
            "SELECT column_classroom_ID, column_classroom_name FROM table_classroom_names",
    )
    connection.execSQL(
        "INSERT INTO table_names(column_name_ID, column_name_name, column_group_id) " +
            "SELECT column_pupil_ID, column_pupil_name, column_classroom_ID FROM table_pupil_names",
    )
    connection.carrySequence(from = "table_classroom_names", to = "table_groups")
    connection.carrySequence(from = "table_pupil_names", to = "table_names")
    connection.execSQL("DROP TABLE IF EXISTS table_pupil_names")
    connection.execSQL("DROP TABLE IF EXISTS table_classroom_names")
}

/**
 * Rebuilds the v2 tables as Room-describable v3 tables. Room can't describe the legacy
 * `boolean` column or the untyped group id column, so the tables are recreated, not altered.
 *
 * - Ids, text and group membership are kept exactly.
 * - `selected` is what 2.7.0 displayed: it read `cursor.getInt(toggled) > 0`, so NULL or the
 *   text 'false' (an old-SQLite `DEFAULT false`) become 0.
 * - Names whose group doesn't exist, or whose group id is NULL, are dropped (the legacy foreign
 *   key was never enforced). The group id is taken from the groups side of the join.
 * - `drawn` starts at 0.
 */
internal val MIGRATION_2_3 = Migration(2, 3) { connection ->
    // DDL copied from schemas/…NamesDatabase/3.json, so Room's validation matches.
    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `name_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`label` TEXT NOT NULL)",
    )
    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `names` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`group_id` INTEGER NOT NULL, `text` TEXT NOT NULL, " +
            "`selected` INTEGER NOT NULL DEFAULT 0, `drawn` INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY(`group_id`) REFERENCES `name_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    )
    connection.execSQL("CREATE INDEX IF NOT EXISTS `index_names_group_id` ON `names` (`group_id`)")

    connection.execSQL(
        "INSERT INTO name_groups(id, label) " +
            "SELECT column_group_id, column_group_name FROM table_groups",
    )
    connection.execSQL(
        "INSERT INTO names(id, group_id, text, selected, drawn) " +
            "SELECT n.column_name_ID, g.column_group_id, n.column_name_name, " +
            "CASE WHEN CAST(n.column_name_toggled AS INTEGER) > 0 THEN 1 ELSE 0 END, 0 " +
            "FROM table_names n INNER JOIN table_groups g ON g.column_group_id = n.column_group_id",
    )
    connection.carrySequence(from = "table_groups", to = "name_groups")
    connection.carrySequence(from = "table_names", to = "names")
    connection.execSQL("DROP TABLE table_names")
    connection.execSQL("DROP TABLE table_groups")
}

internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

/**
 * Raises [to]'s AUTOINCREMENT sequence to at least [from]'s, before [from] is dropped.
 *
 * Copying rows with explicit ids only lifts the new sequence to the highest *surviving* id;
 * dropping the old table then forgets ids of rows deleted earlier. 2.7.0 kept per-group
 * preferences keyed by group id and never removed them, so a reused id would inherit a
 * deleted group's settings.
 */
private fun SQLiteConnection.carrySequence(from: String, to: String) {
    // Safety net only: every caller runs an INSERT … SELECT into [to] first, and SQLite creates
    // the sqlite_sequence row on any INSERT into an AUTOINCREMENT table, even one that copies
    // zero rows. Kept so this helper stays correct if a migration ever calls it differently.
    execSQL(
        "INSERT INTO sqlite_sequence(name, seq) SELECT '$to', 0 " +
            "WHERE NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = '$to')",
    )
    execSQL(
        "UPDATE sqlite_sequence SET seq = MAX(seq, " +
            "IFNULL((SELECT seq FROM sqlite_sequence WHERE name = '$from'), 0)) WHERE name = '$to'",
    )
}
