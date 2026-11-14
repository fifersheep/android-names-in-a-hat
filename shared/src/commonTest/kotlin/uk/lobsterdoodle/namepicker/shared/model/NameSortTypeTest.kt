package uk.lobsterdoodle.namepicker.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals

class NameSortTypeTest {
    @Test
    fun `sort type names are a stored format`() {
        // 2.7.0 writes these names to preferences; renaming them loses users' sort orders.
        assertEquals(listOf("NONE", "ASC", "DESC"), NameSortType.entries.map { it.name })
    }

    @Test
    fun `next cycles NONE to ASC to DESC and back to NONE`() {
        // 3.0 spec D17: Unsorted -> A-Z -> Z-A -> Unsorted (2.7.0 went DESC -> ASC).
        assertEquals(NameSortType.ASC, NameSortType.NONE.next())
        assertEquals(NameSortType.DESC, NameSortType.ASC.next())
        assertEquals(NameSortType.NONE, NameSortType.DESC.next())
    }

    @Test
    fun `fromStored reads known values`() {
        assertEquals(NameSortType.NONE, NameSortType.fromStored("NONE"))
        assertEquals(NameSortType.ASC, NameSortType.fromStored("ASC"))
        assertEquals(NameSortType.DESC, NameSortType.fromStored("DESC"))
    }

    @Test
    fun `fromStored reads unknown or missing values as NONE`() {
        assertEquals(NameSortType.NONE, NameSortType.fromStored(null))
        assertEquals(NameSortType.NONE, NameSortType.fromStored(""))
        assertEquals(NameSortType.NONE, NameSortType.fromStored("asc"))
        assertEquals(NameSortType.NONE, NameSortType.fromStored("SIDEWAYS"))
    }
}
