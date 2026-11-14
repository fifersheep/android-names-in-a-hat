package uk.lobsterdoodle.namepicker.ui.draw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Spec "Reveal": 250 ms apart, shrinking past 4 names so the last card starts by 1.5 s. */
class RevealStaggerTest {
    @Test
    fun `one name has the default stagger and no division by zero`() {
        assertEquals(250f, revealStaggerMs(1))
    }

    @Test
    fun `up to four names are 250 ms apart`() {
        assertEquals(250f, revealStaggerMs(4))
    }

    @Test
    fun `more names start the last card by 1500 ms`() {
        for (n in 5..60) {
            val lastStart = 600 + (n - 1) * revealStaggerMs(n)
            assertTrue(lastStart <= 1500f + 0.01f, "n=$n starts the last card at $lastStart")
        }
        assertEquals(225f, revealStaggerMs(5))
    }
}
