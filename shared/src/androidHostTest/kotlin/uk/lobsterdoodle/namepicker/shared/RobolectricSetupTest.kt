package uk.lobsterdoodle.namepicker.shared

import android.os.Build
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Guards the host-test setup: if `robolectric.properties` stops being picked up, Robolectric
 * falls back to an SDK whose sandbox needs JDK 21 and every Android host test breaks obscurely.
 */
@RunWith(RobolectricTestRunner::class)
class RobolectricSetupTest {
    @Test
    fun `robolectric runs on the pinned sdk`() {
        assertEquals(34, Build.VERSION.SDK_INT)
    }
}
