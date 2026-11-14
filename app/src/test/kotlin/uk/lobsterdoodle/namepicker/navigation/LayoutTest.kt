package uk.lobsterdoodle.namepicker.navigation

import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Spec "Large screens" (D16): list-detail only at width >= 840 dp and height >= 480 dp. */
class LayoutTest {

    private fun size(widthDp: Int, heightDp: Int) =
        WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(widthDp, heightDp)

    @Test
    fun `medium width is single pane`() = assertFalse(isListDetail(size(839, 600)))

    @Test
    fun `expanded width with compact height is single pane`() = assertFalse(isListDetail(size(840, 479)))

    @Test
    fun `a phone in landscape is single pane`() = assertFalse(isListDetail(size(915, 412)))

    @Test
    fun `expanded width and medium height is list-detail`() = assertTrue(isListDetail(size(840, 480)))

    @Test
    fun `a tablet in landscape is list-detail`() = assertTrue(isListDetail(size(1280, 800)))

    @Test
    fun `the list pane is 360 dp below 1600 dp`() = assertEquals(360.dp, listPaneWidth(size(1599, 900)))

    @Test
    fun `the list pane is 400 dp from 1600 dp`() = assertEquals(400.dp, listPaneWidth(size(1600, 900)))
}
