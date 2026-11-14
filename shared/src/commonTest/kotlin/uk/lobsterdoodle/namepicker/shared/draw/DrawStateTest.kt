package uk.lobsterdoodle.namepicker.shared.draw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import uk.lobsterdoodle.namepicker.shared.model.Name

/** Spec "Select-all header", "Draw bar" and "Draw from remaining": in the hat = selected and not drawn. */
class DrawStateTest {
    @Test
    fun `the minimum to draw is one`() {
        assertEquals(1, MIN_NAMES_TO_DRAW)
    }

    @Test
    fun `counts many in the hat`() {
        val state = DrawState.of(listOf(on(), on(), on(), off()))
        assertEquals(3, state.inHat)
        assertEquals(4, state.total)
        assertEquals(0, state.drawnCount)
    }

    @Test
    fun `counts one in the hat`() {
        val state = DrawState.of(listOf(on(), off(), off()))
        assertEquals(1, state.inHat)
        assertEquals(3, state.total)
    }

    @Test
    fun `counts none in the hat`() {
        val state = DrawState.of(listOf(off(), off()))
        assertEquals(0, state.inHat)
        assertEquals(2, state.total)
    }

    @Test
    fun `counts a list of one`() {
        val state = DrawState.of(listOf(on()))
        assertEquals(1, state.inHat)
        assertEquals(1, state.total)
        assertEquals(SelectAllBox.ALL, state.box)
        assertEquals(DrawBarState.Stepper(max = 1), state.bar)
    }

    @Test
    fun `drawn names are counted but are not in the hat`() {
        val state = DrawState.of(listOf(on(), on(drawn = true), off(drawn = true), off()))
        assertEquals(1, state.inHat)
        assertEquals(4, state.total)
        assertEquals(2, state.drawnCount)
    }

    @Test
    fun `box is all when every not-drawn name is selected`() {
        assertEquals(SelectAllBox.ALL, DrawState.of(listOf(on(), on())).box)
        // A drawn name's selection is invisible on its chip so it does not count.
        assertEquals(SelectAllBox.ALL, DrawState.of(listOf(on(), off(drawn = true))).box)
    }

    @Test
    fun `box is some when some not-drawn names are selected`() {
        assertEquals(SelectAllBox.SOME, DrawState.of(listOf(on(), off())).box)
        assertEquals(SelectAllBox.SOME, DrawState.of(listOf(on(), off(), on(drawn = true))).box)
    }

    @Test
    fun `box is none when no not-drawn name is selected`() {
        assertEquals(SelectAllBox.NONE, DrawState.of(listOf(off(), off())).box)
        assertEquals(SelectAllBox.NONE, DrawState.of(listOf(off(), on(drawn = true))).box)
    }

    @Test
    fun `box is disabled when every name is drawn`() {
        assertEquals(SelectAllBox.DISABLED, DrawState.of(listOf(on(drawn = true), off(drawn = true))).box)
    }

    @Test
    fun `the stepper goes up to the number in the hat`() {
        val state = DrawState.of(listOf(on(), on(), on(drawn = true), off()))
        assertEquals(DrawBarState.Stepper(max = 2), state.bar)
        assertTrue(state.canDraw)
    }

    @Test
    fun `nothing in the hat and nothing selected drawn asks for names`() {
        val state = DrawState.of(listOf(off(), off(drawn = true)))
        assertEquals(DrawBarState.NothingInHat, state.bar)
        assertFalse(state.canDraw)
    }

    @Test
    fun `nothing in the hat after every selected name is drawn says so`() {
        val state = DrawState.of(listOf(on(drawn = true), off()))
        assertEquals(DrawBarState.AllDrawn, state.bar)
        assertFalse(state.canDraw)
    }

    @Test
    fun `an empty list has no names and cannot draw`() {
        val state = DrawState.of(emptyList())
        assertFalse(state.hasNames)
        assertEquals(0, state.inHat)
        assertEquals(0, state.total)
        assertFalse(state.canDraw)
        assertEquals(DrawBarState.NothingInHat, state.bar)
    }

    @Test
    fun `a list with names has names`() {
        assertTrue(DrawState.of(listOf(off())).hasNames)
    }

    @Test
    fun `clampCount keeps a count within one to the number in the hat`() {
        assertEquals(1, clampCount(1, inHat = 5))
        assertEquals(4, clampCount(4, inHat = 5))
        assertEquals(5, clampCount(9, inHat = 5))
        assertEquals(1, clampCount(0, inHat = 5))
        // With nothing in the hat the count rests at 1 so it is ready when names return.
        assertEquals(1, clampCount(3, inHat = 0))
    }

    private fun on(drawn: Boolean = false) = Name(id = 0, groupId = 1, text = "name", selected = true, drawn = drawn)
    private fun off(drawn: Boolean = false) = Name(id = 0, groupId = 1, text = "name", selected = false, drawn = drawn)
}
