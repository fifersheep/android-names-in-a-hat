package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import uk.lobsterdoodle.namepicker.shared.fakes.FakePreferencesRepository
import uk.lobsterdoodle.namepicker.shared.selection.GridColumns

class GridLayoutTest {
    @Test
    fun `selecting 1 stores 1`() = runTest {
        val prefs = FakePreferencesRepository()
        GridLayout(prefs).select(1)
        assertEquals(listOf("setGridColumns(1)"), prefs.calls)
    }

    @Test
    fun `selecting 2 stores 2`() = runTest {
        val prefs = FakePreferencesRepository(gridColumns = 1)
        GridLayout(prefs).select(2)
        assertEquals(listOf("setGridColumns(2)"), prefs.calls)
    }

    @Test
    fun `observes the stored count with its toggle target`() = runTest {
        val prefs = FakePreferencesRepository(gridColumns = 1)
        assertEquals(GridColumns(current = 1, next = 2), GridLayout(prefs).observe().first())
    }
}
