package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Rule
import org.junit.Test
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.model.Name

/**
 * Upgrades from the databases 2.7.0 and earlier left on devices. Losing a user's groups here is
 * the worst outcome of 3.0, so every fixture is checked row by row, both with Room's
 * MigrationTestHelper (schema validation against schemas/…/3.json) and end to end through
 * the production builder and repositories.
 */
class MigrationTest {
    private val dir: File = Files.createTempDirectory("nam5-migration").toFile()
    private val dbFile = File(dir, DATABASE_NAME)

    @get:Rule
    val helper = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"),
        databasePath = dbFile.toPath(),
        driver = BundledSQLiteDriver(),
        databaseClass = NamesDatabase::class,
        databaseFactory = { NamesDatabaseConstructor.initialize() },
    )

    @After
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun `the database file is the one 2_7_0 wrote`() {
        assertEquals("Classrooms.db", DATABASE_NAME)
    }

    // --- MigrationTestHelper: run the migrations, validate the v3 schema, read the raw rows ---

    @Test
    fun `clean v2 migrates with every id text group and selection kept`() = migrateAndValidate(CLEAN_V2)

    @Test
    fun `v1 migrates through v2 with orphans dropped`() = migrateAndValidate(V1)

    @Test
    fun `dirty v2 migrates without throwing and normalises selection`() = migrateAndValidate(DIRTY_V2)

    @Test
    fun `emptied v2 keeps its id sequences with no rows left`() = migrateAndValidate(EMPTIED_V2)

    private fun migrateAndValidate(fixture: LegacyFixture) = runBlocking {
        fixture.writeTo(dbFile)

        val connection = helper.runMigrationsAndValidate(3, ALL_MIGRATIONS.toList())

        assertEquals(fixture.expectedGroups.toSet(), connection.groups(), "$fixture groups")
        assertEquals(fixture.expectedNames, connection.names(), "$fixture names (drawn must all be false)")
        assertEquals(setOf("name_groups", "names", "android_metadata"), connection.userTables(), "$fixture tables")
        assertTrue(connection.sequence("name_groups") >= fixture.groupSeq, "$fixture group sequence carried over")
        assertTrue(connection.sequence("names") >= fixture.nameSeq, "$fixture name sequence carried over")
    }

    // --- End to end: production builder and repositories, as the app will open the file ---

    @Test
    fun `clean v2 opens end to end through the repositories`() = openEndToEnd(CLEAN_V2)

    @Test
    fun `v1 opens end to end through the repositories`() = openEndToEnd(V1)

    @Test
    fun `dirty v2 opens end to end through the repositories`() = openEndToEnd(DIRTY_V2)

    @Test
    fun `emptied v2 opens end to end and does not reuse deleted ids`() = openEndToEnd(EMPTIED_V2)

    private fun openEndToEnd(fixture: LegacyFixture) = runBlocking {
        withTimeout(10_000) {
            fixture.writeTo(dbFile)
            val storage = openStorage(dir)
            try {
                val expectedSummaries = fixture.expectedGroups
                    .sortedByDescending { it.id }
                    .map { group -> GroupSummary(group, fixture.expectedNames.count { it.groupId == group.id }) }
                assertEquals(expectedSummaries, storage.groups.observeGroups().first(), "$fixture groups newest first")

                for (group in fixture.expectedGroups) {
                    assertEquals(
                        fixture.expectedNames.filter { it.groupId == group.id }.sortedBy { it.id },
                        storage.names.observeNames(group.id).first(),
                        "$fixture names of group ${group.id} in insertion order",
                    )
                }

                // Ids are never reused, even those deleted before the upgrade.
                val newGroup = storage.groups.create("New")
                assertTrue(newGroup > fixture.groupSeq, "$fixture new group id $newGroup > ${fixture.groupSeq}")
                val newName = storage.names.add(newGroup, "New", selected = true)
                assertTrue(newName > fixture.nameSeq, "$fixture new name id $newName > ${fixture.nameSeq}")
            } finally {
                storage.close()
            }
        }
    }

    @Test
    fun `a migrated database reopens without migrating again`() = runBlocking {
        CLEAN_V2.writeTo(dbFile)
        openStorage(dir).use { it.groups.observeGroups().first() }

        openStorage(dir).use { storage ->
            assertEquals(
                CLEAN_V2.expectedGroups.sortedByDescending { it.id },
                storage.groups.observeGroups().first().map { it.group },
            )
        }
    }
}

private fun SQLiteConnection.groups(): Set<Group> = query("SELECT id, label FROM name_groups") {
    Group(getLong(0), getText(1))
}.toSet()

private fun SQLiteConnection.names(): Set<Name> =
    query("SELECT id, group_id, text, selected, drawn FROM names") {
        Name(getLong(0), getLong(1), getText(2), selected = getLong(3) != 0L, drawn = getLong(4) != 0L)
    }.toSet()

private fun SQLiteConnection.userTables(): Set<String> =
    query("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name != 'room_master_table'") {
        getText(0)
    }.toSet()

private fun SQLiteConnection.sequence(table: String): Long =
    query("SELECT seq FROM sqlite_sequence WHERE name = '$table'") { getLong(0) }.single()

private fun <T> SQLiteConnection.query(sql: String, row: androidx.sqlite.SQLiteStatement.() -> T): List<T> {
    val statement = prepare(sql)
    try {
        val rows = mutableListOf<T>()
        while (statement.step()) rows += statement.row()
        return rows
    } finally {
        statement.close()
    }
}
