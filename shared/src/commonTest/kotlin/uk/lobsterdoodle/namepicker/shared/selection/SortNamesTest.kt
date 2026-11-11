package uk.lobsterdoodle.namepicker.shared.selection

import kotlin.test.Test
import kotlin.test.assertEquals
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

class SortNamesTest {
    private val unsorted = listOf(name(2, "kim"), name(1, "Bauer"), name(3, "Yelena"))

    @Test
    fun `NONE keeps order`() {
        assertEquals(unsorted, sortNames(unsorted, NameSortType.NONE))
    }

    @Test
    fun `ASC ignores case`() {
        assertEquals(listOf(name(1, "Bauer"), name(2, "kim"), name(3, "Yelena")), sortNames(unsorted, NameSortType.ASC))
    }

    @Test
    fun `DESC ignores case`() {
        assertEquals(listOf(name(3, "Yelena"), name(2, "kim"), name(1, "Bauer")), sortNames(unsorted, NameSortType.DESC))
    }

    @Test
    fun `ASC keeps ties in input order`() {
        // "ada" and "Ada" compare equal ignoring case; a stable sort keeps them as given.
        val names = listOf(name(5, "zed"), name(7, "ada"), name(6, "Ada"))
        assertEquals(listOf(7L, 6L, 5L), sortNames(names, NameSortType.ASC).map { it.id })
        assertEquals(listOf(5L, 7L, 6L), sortNames(names, NameSortType.DESC).map { it.id })
    }

    private fun name(id: Long, text: String) = Name(id, groupId = 24, text = text, selected = false)
}
