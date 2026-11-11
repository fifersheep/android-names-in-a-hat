package uk.lobsterdoodle.namepicker.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun appNameMatchesThePlayListing() {
        assertEquals("NAM-3 deliberate red proof", AppInfo.NAME)
    }
}
