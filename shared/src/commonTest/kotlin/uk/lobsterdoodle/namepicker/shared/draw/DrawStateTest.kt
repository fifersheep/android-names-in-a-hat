package uk.lobsterdoodle.namepicker.shared.draw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import uk.lobsterdoodle.namepicker.shared.model.Name

class DrawStateTest {
    @Test
    fun `some selected`() {
        val state = DrawState.of(listOf(on(), on(), off(), off()))
        assertEquals(2, state.selectedCount)
        assertEquals(4, state.totalCount)
        assertEquals(listOf(1, 2), state.options)
        assertFalse(state.allSelected)
    }

    @Test
    fun `all selected`() {
        val state = DrawState.of(listOf(on(), on(), on(), on()))
        assertEquals(4, state.selectedCount)
        assertEquals(4, state.totalCount)
        assertEquals(listOf(1, 2, 3, 4), state.options)
        assertTrue(state.allSelected)
    }

    @Test
    fun `none selected`() {
        val state = DrawState.of(listOf(off(), off(), off(), off()))
        assertEquals(0, state.selectedCount)
        assertEquals(4, state.totalCount)
        // Legacy offered a single "0" option here; zero is not a drawable count.
        assertEquals(emptyList(), state.options)
        assertFalse(state.allSelected)
    }

    @Test
    fun `cannot draw below the minimum`() {
        val state = DrawState.of(listOf(on(), off(), off()))
        assertFalse(state.canDraw)
    }

    @Test
    fun `can draw at the minimum`() {
        val state = DrawState.of(List(MIN_NAMES_TO_DRAW) { on() } + off())
        assertTrue(state.canDraw)
    }

    @Test
    fun `hasNames false when empty`() {
        val state = DrawState.of(emptyList())
        assertFalse(state.hasNames)
        // Legacy labelled an empty group "Clear All" (0 == 0); an empty group is not all selected.
        assertFalse(state.allSelected)
        assertFalse(state.canDraw)
    }

    @Test
    fun `hasNames true with names`() {
        assertTrue(DrawState.of(listOf(off())).hasNames)
    }

    private fun on() = Name(id = 0, groupId = 1, text = "name", selected = true)
    private fun off() = Name(id = 0, groupId = 1, text = "name", selected = false)
}
