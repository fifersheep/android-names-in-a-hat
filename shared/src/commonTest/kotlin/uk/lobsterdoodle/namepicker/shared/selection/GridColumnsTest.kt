package uk.lobsterdoodle.namepicker.shared.selection

import kotlin.test.Test
import kotlin.test.assertEquals

class GridColumnsTest {
    @Test
    fun `one column toggles to two`() {
        assertEquals(GridColumns(current = 1, next = 2), GridColumns.of(1))
    }

    @Test
    fun `two toggles to one`() {
        assertEquals(GridColumns(current = 2, next = 1), GridColumns.of(2))
    }

    @Test
    fun `out-of-range stored columns read as 2`() {
        assertEquals(GridColumns(current = 2, next = 1), GridColumns.of(0))
        assertEquals(GridColumns(current = 2, next = 1), GridColumns.of(3))
        assertEquals(GridColumns(current = 2, next = 1), GridColumns.of(-1))
    }

    @Test
    fun `the default is two columns`() {
        assertEquals(2, DEFAULT_GRID_COLUMNS)
    }
}
