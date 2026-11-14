package uk.lobsterdoodle.namepicker.shared.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

/**
 * The production `SettingsStorage(context)` against real Android framework SharedPreferences
 * (Robolectric): 2.7.0's `name_list` settings carry over on first use, the retired column count
 * and 1.x leftovers do not, and the old file is deleted.
 */
@RunWith(RobolectricTestRunner::class)
class LegacyNameListMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val legacyFile get() = File(context.dataDir, "shared_prefs/name_list.xml")
    private val settingsFile get() = File(context.filesDir, "datastore/$SETTINGS_FILE_NAME")
    private var settings: SettingsStorage? = null

    @After
    fun close() {
        runBlocking { settings?.closeAndJoin() }
    }

    private fun open(): SettingsStorage = SettingsStorage(context).also { settings = it }

    private fun test(block: suspend CoroutineScope.() -> Unit) = runBlocking { withTimeout(10_000, block) }

    private suspend fun SettingsStorage.storedKeys(): Set<String> =
        dataStore.data.first().asMap().keys.map { it.name }.toSet()

    private fun seedLegacy(edit: android.content.SharedPreferences.Editor.() -> Unit) {
        val prefs = context.getSharedPreferences("name_list", Context.MODE_PRIVATE)
        assertTrue(prefs.edit().apply(edit).commit())
        assertTrue(legacyFile.exists(), "the legacy file should be on disk before migrating")
    }

    @Test
    fun `2_7_0 settings carry over and the old file is deleted`() = test {
        seedLegacy {
            putLong("active_group_id", 7)
            putLong("current_grid_column_count", 1)
            putString("group_sort_type_for_group_3", "ASC")
            putString("group_sort_type_for_group_4", "DESC")
        }

        val settings = open()
        val prefs = settings.preferences

        assertEquals(7L, prefs.activeGroupId.first())
        assertEquals(NameSortType.ASC, prefs.sortType(3).first())
        assertEquals(NameSortType.DESC, prefs.sortType(4).first())
        assertEquals(
            setOf("active_group_id", "group_sort_type_for_group_3", "group_sort_type_for_group_4"),
            settings.storedKeys(),
        )
        assertFalse(legacyFile.exists(), "name_list.xml should be deleted")
        assertTrue(settingsFile.exists(), "settings should be written to $settingsFile")
    }

    @Test
    fun `a 2_7_0 name_list file on disk carries over`() = test {
        // Exactly what 2.7.0's SharedPreferences writes; placed before anything opens the file.
        legacyFile.parentFile!!.mkdirs()
        legacyFile.writeText(
            """
            <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
            <map>
                <long name="active_group_id" value="12" />
                <long name="current_grid_column_count" value="1" />
                <string name="group_sort_type_for_group_12">DESC</string>
                <string name="group_sort_type_for_group_5">ASC</string>
            </map>
            """.trimIndent(),
        )

        val settings = open()
        val prefs = settings.preferences

        assertEquals(12L, prefs.activeGroupId.first())
        assertEquals(NameSortType.DESC, prefs.sortType(12).first())
        assertEquals(NameSortType.ASC, prefs.sortType(5).first())
        assertEquals(
            setOf("active_group_id", "group_sort_type_for_group_12", "group_sort_type_for_group_5"),
            settings.storedKeys(),
        )
        assertFalse(legacyFile.exists(), "name_list.xml should be deleted")
    }

    @Test
    fun `without an old file the defaults apply`() = test {
        val prefs = open().preferences

        assertNull(prefs.activeGroupId.first())
        assertEquals(NameSortType.NONE, prefs.sortType(3).first())
        assertEquals(2, prefs.gridColumns.first())
        assertFalse(legacyFile.exists())
    }

    @Test
    fun `an unrecognised migrated sort value reads as NONE`() = test {
        seedLegacy { putString("group_sort_type_for_group_3", "SHUFFLE") }

        assertEquals(NameSortType.NONE, open().preferences.sortType(3).first())
        assertFalse(legacyFile.exists())
    }

    @Test
    fun `a file holding only retired and 1_x keys copies nothing and is still deleted`() = test {
        seedLegacy {
            putLong("current_grid_column_count", 1)
            putString("current_classroom", "Class 4B")
            putStringSet("names", setOf("Ann", "Bob"))
        }

        val settings = open()

        assertNull(settings.preferences.activeGroupId.first())
        assertTrue(settings.storedKeys().isEmpty(), "nothing should be copied: ${settings.storedKeys()}")
        assertFalse(legacyFile.exists(), "name_list.xml should be deleted")
    }

    @Test
    fun `a reappearing old file never overwrites newer settings`() = test {
        open().also { it.preferences.setActiveGroup(3) }.closeAndJoin()
        settings = null
        // e.g. a 2.x backup restored after the upgrade.
        seedLegacy {
            putLong("active_group_id", 9)
            putString("group_sort_type_for_group_9", "ASC")
        }

        val prefs = open().preferences

        assertEquals(3L, prefs.activeGroupId.first())
        assertEquals(NameSortType.ASC, prefs.sortType(9).first())
        assertFalse(legacyFile.exists())
    }
}
