package uk.lobsterdoodle.namepicker.shared.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LabelsTest {
    @Test
    fun `empty label is rejected`() {
        assertNull(validateLabel(""))
    }

    @Test
    fun `spaces only label is rejected`() {
        assertNull(validateLabel("   "))
    }

    @Test
    fun `tabs only label is rejected`() {
        assertNull(validateLabel("\t\t"))
    }

    @Test
    fun `line breaks only label is rejected`() {
        assertNull(validateLabel("\n"))
        assertNull(validateLabel("\r"))
        assertNull(validateLabel("\r\n"))
        assertNull(validateLabel(" \t\r\n "))
    }

    @Test
    fun `label is trimmed`() {
        assertEquals("Ada", validateLabel("  Ada  "))
        assertEquals("Ada", validateLabel("\tAda\n"))
    }

    @Test
    fun `inner whitespace is kept`() {
        assertEquals("Ada Lovelace", validateLabel(" Ada Lovelace "))
    }

    @Test
    fun `inner line breaks and tabs in a name become one space`() {
        assertEquals("Ada Ben", normaliseName("Ada\nBen"))
        assertEquals("Ada Ben", normaliseName("Ada\tBen"))
        assertEquals("Ada Ben", normaliseName("Ada\r\nBen"))
        assertEquals("Ada Ben", normaliseName("Ada\u2028Ben"))
        assertEquals("Ada Ben", normaliseName("Ada\u2029Ben"))
        assertEquals("Ada Ben", normaliseName("Ada\u000BBen"))
        assertEquals("Ada Ben", normaliseName("Ada\u000CBen"))
        assertEquals("Ada Ben", normaliseName("Ada\u0085Ben"))
    }

    @Test
    fun `a run of line breaks and tabs becomes a single space`() {
        assertEquals("Ada Ben", normaliseName("Ada\n\n\t\r\nBen"))
    }

    @Test
    fun `normalising leaves ordinary spaces alone`() {
        assertEquals("Ada  Lovelace", normaliseName("Ada  Lovelace"))
    }

    @Test
    fun `validateName normalises then trims`() {
        assertEquals("Ada Ben", validateName(" \tAda\nBen\n "))
    }

    @Test
    fun `validateName rejects line breaks and tabs only`() {
        assertNull(validateName("\n\t\r\n\u2028"))
    }
}
