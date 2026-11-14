package uk.lobsterdoodle.namepicker.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/** Blank list names, which 2.7.0 allowed (GitHub #6), display as "Untitled list". */
class ListLabelTest {

    @Test
    fun `a named list shows its name`() = assertEquals("Year 5", displayLabel("Year 5", untitled = "Untitled list"))

    @Test
    fun `an empty name shows the untitled label`() = assertEquals("Untitled list", displayLabel("", untitled = "Untitled list"))

    @Test
    fun `a whitespace-only name shows the untitled label`() =
        assertEquals("Untitled list", displayLabel(" \t\n", untitled = "Untitled list"))

    @Test
    fun `a blank name shows the unnamed label`() = assertEquals("Unnamed", displayLabel(" ", untitled = "Unnamed"))
}
