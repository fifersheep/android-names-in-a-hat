package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import uk.lobsterdoodle.namepicker.shared.fakes.FakeGroupRepository
import uk.lobsterdoodle.namepicker.shared.fakes.FakePreferencesRepository
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

class GroupEditorTest {
    private val groups = FakeGroupRepository(listOf(Group(24, "CTU"), Group(25, "Division")))
    private val prefs = FakePreferencesRepository(
        sortTypes = mapOf(24L to NameSortType.DESC, 25L to NameSortType.ASC),
    )
    private val editor = GroupEditor(groups, prefs)

    @Test
    fun `create stores the trimmed label and returns its id`() = runTest {
        val result = editor.create("  Year 7  ")
        val id = (result as WriteResult.Saved).value
        assertEquals(listOf("create(Year 7)"), groups.calls)
        assertEquals(Group(id, "Year 7"), groups.observeGroup(id).first())
    }

    @Test
    fun `create rejects a blank label and writes nothing`() = runTest {
        assertEquals(WriteResult.Blank, editor.create(" \t\r\n "))
        assertEquals(emptyList(), groups.calls)
    }

    @Test
    fun `rename stores the trimmed label`() = runTest {
        assertEquals(WriteResult.Saved(Unit), editor.rename(24, "\tCTU LA\n"))
        assertEquals(Group(24, "CTU LA"), groups.observeGroup(24).first())
    }

    @Test
    fun `rename rejects a blank label and writes nothing`() = runTest {
        assertEquals(WriteResult.Blank, editor.rename(24, "   "))
        assertEquals(emptyList(), groups.calls)
        assertEquals(Group(24, "CTU"), groups.observeGroup(24).first())
    }

    @Test
    fun `delete returns the deleted group and removes its sort order`() = runTest {
        assertEquals(Group(24, "CTU"), editor.delete(24))
        assertNull(groups.observeGroup(24).first())
        assertEquals(mapOf(25L to NameSortType.ASC), prefs.sorts.value)
    }

    @Test
    fun `deleting the active group clears it`() = runTest {
        prefs.activeGroup.value = 24
        editor.delete(24)
        assertNull(prefs.activeGroup.value)
    }

    @Test
    fun `deleting a group clears the active group with one conditional write`() = runTest {
        editor.delete(24)
        // A read-then-clear could wipe a group opened in between; the conditional clear cannot.
        assertEquals(listOf("removeSortType(24)", "clearActiveGroupIf(24)"), prefs.calls)
    }

    @Test
    fun `deleting another group keeps the active group`() = runTest {
        prefs.activeGroup.value = 25
        editor.delete(24)
        assertEquals(25L, prefs.activeGroup.value)
    }

    @Test
    fun `deleting a missing group changes no preferences`() = runTest {
        prefs.activeGroup.value = 99
        assertNull(editor.delete(99))
        assertEquals(emptyList(), prefs.calls)
    }
}
