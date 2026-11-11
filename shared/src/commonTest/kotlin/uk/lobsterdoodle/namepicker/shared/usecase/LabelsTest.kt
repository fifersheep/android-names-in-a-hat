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
}
