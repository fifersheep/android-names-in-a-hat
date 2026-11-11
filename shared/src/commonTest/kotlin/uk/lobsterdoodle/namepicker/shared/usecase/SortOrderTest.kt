package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import uk.lobsterdoodle.namepicker.shared.fakes.FakePreferencesRepository
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.selection.sortNames

class SortOrderTest {
    @Test
    fun `cycle from NONE stores ASC`() = runTest {
        val prefs = FakePreferencesRepository()
        SortOrder(prefs).cycle(24)
        assertEquals(NameSortType.ASC, prefs.sorts.value[24L])
    }

    @Test
    fun `cycle from ASC stores DESC`() = runTest {
        val prefs = FakePreferencesRepository(sortTypes = mapOf(24L to NameSortType.ASC))
        SortOrder(prefs).cycle(24)
        assertEquals(NameSortType.DESC, prefs.sorts.value[24L])
    }

    @Test
    fun `cycle from DESC stores ASC`() = runTest {
        val prefs = FakePreferencesRepository(sortTypes = mapOf(24L to NameSortType.DESC))
        SortOrder(prefs).cycle(24)
        assertEquals(NameSortType.ASC, prefs.sorts.value[24L])
    }

    @Test
    fun `cycle returns the new order that sorts the names`() = runTest {
        val names = listOf(name(2, "kim"), name(1, "Bauer"), name(3, "Yelena"))
        val order = SortOrder(FakePreferencesRepository()).cycle(24)
        assertEquals(NameSortType.ASC, order)
        assertEquals(listOf(1L, 2L, 3L), sortNames(names, order).map { it.id })
    }

    @Test
    fun `cycle uses one atomic update`() = runTest {
        val prefs = FakePreferencesRepository()
        SortOrder(prefs).cycle(24)
        assertEquals(listOf("updateSortType(24) -> ASC"), prefs.calls)
    }

    @Test
    fun `observe reads the stored order for the group`() = runTest {
        val prefs = FakePreferencesRepository(sortTypes = mapOf(24L to NameSortType.DESC))
        assertEquals(NameSortType.DESC, SortOrder(prefs).observe(24).first())
        assertEquals(NameSortType.NONE, SortOrder(prefs).observe(25).first())
    }

    private fun name(id: Long, text: String) = Name(id, groupId = 24, text = text, selected = false)
}
