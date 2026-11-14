package uk.lobsterdoodle.namepicker.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Spec "Navigation" back-stack rules 1–6 and the shell's own operations. */
class BackStackTest {

    private fun stack(vararg keys: NavKey): MutableList<NavKey> = keys.toMutableList()

    @Test
    fun `launch with no active list opens Lists`() {
        assertEquals<List<NavKey>>(listOf(Lists), launchStack(activeListId = null))
    }

    @Test
    fun `launch with an active list seeds Lists under Draw (rule 5)`() {
        assertEquals<List<NavKey>>(listOf(Lists, Draw(7)), launchStack(activeListId = 7))
    }

    @Test
    fun `opening a list from Lists pushes Draw (rule 4)`() {
        val s = stack(Lists)
        s.openList(3)
        assertEquals<List<NavKey>>(listOf(Lists, Draw(3)), s)
    }

    @Test
    fun `opening another list in two-pane replaces the detail`() {
        val s = stack(Lists, Draw(3), EditList(3, session = 1))
        s.openList(4)
        assertEquals<List<NavKey>>(listOf(Lists, Draw(4)), s)
    }

    @Test
    fun `new list pushes an unsaved EditList with a fresh session`() {
        val s = stack(Lists)
        s.newList(session = 11)
        assertEquals<List<NavKey>>(listOf(Lists, EditList(null, session = 11)), s)
    }

    @Test
    fun `new list in two-pane replaces the detail`() {
        val s = stack(Lists, Draw(3))
        s.newList(session = 11)
        assertEquals<List<NavKey>>(listOf(Lists, EditList(null, session = 11)), s)
    }

    @Test
    fun `creating the list replaces EditList(null) and keeps the session (rule 1)`() {
        val s = stack(Lists, EditList(null, session = 11))
        s.listCreated(5)
        assertEquals<List<NavKey>>(listOf(Lists, EditList(5, isNew = true, session = 11)), s)
    }

    @Test
    fun `done on a newly created list replaces it with Draw (rule 2)`() {
        val s = stack(Lists, EditList(5, isNew = true, session = 11))
        s.doneEditing()
        assertEquals<List<NavKey>>(listOf(Lists, Draw(5)), s)
    }

    @Test
    fun `edit from Draw pushes and done pops back to Draw (rules 3 and 4)`() {
        val s = stack(Lists, Draw(3))
        s.editList(3, fromDraw = true, session = 12)
        assertEquals<List<NavKey>>(listOf(Lists, Draw(3), EditList(3, session = 12)), s)
        s.doneEditing()
        assertEquals<List<NavKey>>(listOf(Lists, Draw(3)), s)
    }

    @Test
    fun `edit from Lists truncates then pushes and done pops back to Lists (rule 3)`() {
        val s = stack(Lists, Draw(3))
        s.editList(4, fromDraw = false, session = 13)
        assertEquals<List<NavKey>>(listOf(Lists, EditList(4, session = 13)), s)
        s.doneEditing()
        assertEquals<List<NavKey>>(listOf(Lists), s)
    }

    @Test
    fun `every edit visit gets its own session`() {
        val s = stack(Lists, EditList(1, session = 20))
        s.editList(2, fromDraw = false, session = 21)
        assertEquals<List<NavKey>>(listOf(Lists, EditList(2, session = 21)), s)
    }

    @Test
    fun `back from Draw onto Lists in single pane reports the list left (rule 5)`() {
        val s = stack(Lists, Draw(3))
        assertEquals(3L, s.back(singlePane = true))
        assertEquals<List<NavKey>>(listOf(Lists), s)
    }

    @Test
    fun `back from Draw in two-pane does not count as leaving`() {
        val s = stack(Lists, Draw(3))
        assertNull(s.back(singlePane = false))
        assertEquals<List<NavKey>>(listOf(Lists), s)
    }

    @Test
    fun `back from EditList returns to Draw and does not leave the list`() {
        val s = stack(Lists, Draw(3), EditList(3, session = 12))
        assertNull(s.back(singlePane = true))
        assertEquals<List<NavKey>>(listOf(Lists, Draw(3)), s)
    }

    @Test
    fun `back on Lists alone does nothing`() {
        val s = stack(Lists)
        assertNull(s.back(singlePane = true))
        assertEquals<List<NavKey>>(listOf(Lists), s)
    }

    @Test
    fun `a gone list is removed wherever it appears`() {
        val s = stack(Lists, Draw(3), EditList(3, session = 12))
        s.listGone(3)
        assertEquals<List<NavKey>>(listOf(Lists), s)
    }

    @Test
    fun `a gone list leaves other lists alone`() {
        val s = stack(Lists, Draw(4))
        s.listGone(3)
        assertEquals<List<NavKey>>(listOf(Lists, Draw(4)), s)
    }

    @Test
    fun `new sessions differ`() {
        val sessions = List(100) { newSession() }.toSet()
        assertEquals(100, sessions.size)
    }
}
