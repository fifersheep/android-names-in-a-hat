package uk.lobsterdoodle.namepicker.ui.draw

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.lobsterdoodle.namepicker.ui.theme.NamesTheme

/**
 * NAM-12 AC10 (spec "Chip states", "Accessibility"): a drawn chip says "drawn" and offers "Put back in
 * the hat" as a custom action; In and Out chips don't. uiautomator dumps can't show either, so this
 * checks the Compose semantics directly.
 */
@RunWith(RobolectricTestRunner::class)
class DrawChipSemanticsTest {
    @get:Rule
    val compose = createComposeRule()

    private val putBack = mutableListOf<Long>()
    private val toggled = mutableListOf<Long>()

    private fun show(chip: ChipUi) = compose.setContent {
        NamesTheme { NameChip(chip, onToggle = { toggled += it }, onPutBack = { putBack += it }) }
    }

    private fun node(text: String) = compose.onNodeWithText(text, useUnmergedTree = false).fetchSemanticsNode()

    @Test
    fun `a drawn chip is announced as drawn and offers put back`() {
        show(ChipUi(7, "Chloe", ChipVisual.DRAWN))
        val config = node("Chloe").config
        assertEquals("drawn", config.getOrNull(SemanticsProperties.StateDescription))
        val actions = config.getOrNull(SemanticsActions.CustomActions).orEmpty()
        assertEquals(listOf("Put back in the hat"), actions.map { it.label })
        actions.single().action()
        assertEquals(listOf(7L), putBack)
        assertEquals(emptyList(), toggled)
    }

    @Test
    fun `tapping a drawn chip opens put back and choosing it puts the name back`() {
        show(ChipUi(7, "Chloe", ChipVisual.DRAWN))
        compose.onNodeWithText("Chloe").performClick()
        compose.onNodeWithText("Put back in the hat").performClick()
        assertEquals(listOf(7L), putBack)
        assertEquals(emptyList(), toggled)
    }

    @Test
    fun `an in chip is announced as in the hat with no put back`() {
        show(ChipUi(1, "Ada", ChipVisual.IN))
        val config = node("Ada").config
        assertEquals("in the hat", config.getOrNull(SemanticsProperties.StateDescription))
        assertNull(config.getOrNull(SemanticsActions.CustomActions))
    }

    @Test
    fun `an out chip is announced as out of the hat with no put back`() {
        show(ChipUi(2, "Ben", ChipVisual.OUT))
        val config = node("Ben").config
        assertEquals("out of the hat", config.getOrNull(SemanticsProperties.StateDescription))
        assertNull(config.getOrNull(SemanticsActions.CustomActions))
        compose.onNodeWithText("Ben").performClick()
        assertEquals(listOf(2L), toggled)
    }
}
