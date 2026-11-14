package uk.lobsterdoodle.namepicker.shared.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor

/** [PreferencesRepository] on a real DataStore file, through the production builder. */
class PreferencesRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var settings: SettingsStorage
    private val prefs get() = settings.preferences

    @Before
    fun open() {
        settings = openSettings(tmp.root)
    }

    @After
    fun close() = runBlocking {
        settings.closeAndJoin()
    }

    private fun test(block: suspend CoroutineScope.() -> Unit) = runBlocking { withTimeout(10_000, block) }

    private suspend fun storedKeys(): Set<String> = settings.dataStore.data.first().asMap().keys.map { it.name }.toSet()

    @Test
    fun `a new file has no active group and no sort order`() = test {
        assertNull(prefs.activeGroupId.first())
        assertEquals(NameSortType.NONE, prefs.sortType(1).first())
    }

    @Test
    fun `the retired grid columns read as the default and are never stored`() = test {
        assertEquals(2, prefs.gridColumns.first())
        prefs.setGridColumns(1)
        assertEquals(2, prefs.gridColumns.first())
        assertTrue(storedKeys().isEmpty(), "nothing should be stored: ${storedKeys()}")
    }

    @Test
    fun `the active group is set and cleared under the legacy key`() = test {
        prefs.setActiveGroup(7)
        assertEquals(7L, prefs.activeGroupId.first())
        assertEquals(setOf("active_group_id"), storedKeys())

        prefs.clearActiveGroup()
        assertNull(prefs.activeGroupId.first())
        assertTrue(storedKeys().isEmpty())
    }

    @Test
    fun `clearing the active group only if it matches keeps a different one`() = test {
        prefs.setActiveGroup(7)
        prefs.clearActiveGroupIf(8)
        assertEquals(7L, prefs.activeGroupId.first())

        prefs.clearActiveGroupIf(7)
        assertNull(prefs.activeGroupId.first())
    }

    @Test
    fun `sort orders are stored per group under the legacy key as the constant name`() = test {
        assertEquals(NameSortType.ASC, prefs.updateSortType(3) { it.next() })
        assertEquals(NameSortType.DESC, prefs.updateSortType(4) { NameSortType.DESC })

        assertEquals(NameSortType.ASC, prefs.sortType(3).first())
        assertEquals(NameSortType.DESC, prefs.sortType(4).first())
        assertEquals(NameSortType.NONE, prefs.sortType(5).first())
        val stored = settings.dataStore.data.first()
        assertEquals("ASC", stored[stringPreferencesKey("group_sort_type_for_group_3")])
        assertEquals("DESC", stored[stringPreferencesKey("group_sort_type_for_group_4")])
    }

    @Test
    fun `an unrecognised stored sort value reads as NONE and cycles from there`() = test {
        settings.dataStore.edit { it[stringPreferencesKey("group_sort_type_for_group_3")] = "SHUFFLE" }

        assertEquals(NameSortType.NONE, prefs.sortType(3).first())
        assertEquals(NameSortType.ASC, prefs.updateSortType(3) { it.next() })
    }

    @Test
    fun `removing a sort order deletes its key and keeps the others`() = test {
        prefs.updateSortType(3) { NameSortType.ASC }
        prefs.updateSortType(4) { NameSortType.DESC }

        prefs.removeSortType(3)

        assertEquals(NameSortType.NONE, prefs.sortType(3).first())
        assertEquals(NameSortType.DESC, prefs.sortType(4).first())
        assertEquals(setOf("group_sort_type_for_group_4"), storedKeys())
    }

    @Test
    fun `concurrent sort updates are atomic`() = test {
        val results = coroutineScope {
            (1..50).map { async(Dispatchers.Default) { prefs.updateSortType(9) { it.next() } } }.awaitAll()
        }

        // NONE -> ASC -> DESC -> ASC ...: a lost update would repeat a value and skew the split.
        assertEquals(25, results.count { it == NameSortType.ASC }, "results: $results")
        assertEquals(25, results.count { it == NameSortType.DESC }, "results: $results")
        assertEquals(NameSortType.DESC, prefs.sortType(9).first())
    }

    @Test
    fun `values survive closing and reopening the file`() = test {
        prefs.setActiveGroup(7)
        prefs.updateSortType(3) { NameSortType.DESC }

        settings.closeAndJoin()
        settings = openSettings(tmp.root)

        assertEquals(7L, prefs.activeGroupId.first())
        assertEquals(NameSortType.DESC, prefs.sortType(3).first())
    }

    @Test
    fun `a corrupt file reads as the defaults and can be written again`() = test {
        settings.closeAndJoin()
        File(settingsPath(tmp.root).toString()).writeBytes(byteArrayOf(0x7f, 0x00, 0x13, 0x37, 0x42))
        settings = openSettings(tmp.root)

        assertNull(prefs.activeGroupId.first())
        assertEquals(NameSortType.NONE, prefs.sortType(3).first())
        prefs.setActiveGroup(2)
        assertEquals(2L, prefs.activeGroupId.first())
    }

    @Test
    fun `deleting a group through GroupEditor removes its sort key and clears it as the active group`() = test {
        openStorage(tmp.newFolder("db")).use { storage ->
            val editor = GroupEditor(storage.groups, prefs)
            val first = storage.groups.create("First")
            val second = storage.groups.create("Second")
            prefs.updateSortType(first) { NameSortType.DESC }
            prefs.updateSortType(second) { NameSortType.ASC }
            prefs.setActiveGroup(first)

            editor.delete(first)

            assertNull(prefs.activeGroupId.first())
            assertEquals(NameSortType.NONE, prefs.sortType(first).first())
            assertEquals(NameSortType.ASC, prefs.sortType(second).first())
            assertEquals(setOf("group_sort_type_for_group_$second"), storedKeys())
        }
    }

    @Test
    fun `deleting a group that is not active keeps the active group`() = test {
        openStorage(tmp.newFolder("db")).use { storage ->
            val editor = GroupEditor(storage.groups, prefs)
            val first = storage.groups.create("First")
            val second = storage.groups.create("Second")
            prefs.setActiveGroup(second)

            editor.delete(first)

            assertEquals(second, prefs.activeGroupId.first())
            assertFalse("group_sort_type_for_group_$first" in storedKeys())
        }
    }
}
