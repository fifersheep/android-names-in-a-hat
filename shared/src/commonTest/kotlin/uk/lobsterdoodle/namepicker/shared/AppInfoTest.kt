package uk.lobsterdoodle.namepicker.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun appNameMatchesThePlayListing() {
        assertEquals("Names in a Hat", AppInfo.NAME)
    }
}
