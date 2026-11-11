package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import uk.lobsterdoodle.namepicker.shared.fakes.FakeNameRepository
import uk.lobsterdoodle.namepicker.shared.model.Name

class NameEditorTest {
    private val names = FakeNameRepository(
        listOf(
            Name(1, groupId = 24, text = "Jack", selected = false),
            Name(2, groupId = 24, text = "Chloe", selected = true, drawn = true),
            Name(3, groupId = 24, text = "Tony", selected = false),
            Name(4, groupId = 25, text = "Nina", selected = false),
        ),
    )
    private val editor = NameEditor(names)

    @Test
    fun `add stores the trimmed text as selected`() = runTest {
        editor.add(24, "  Kim  ")
        assertEquals(listOf("add(24, Kim, true)"), names.calls)
    }

    @Test
    fun `add returns Saved with the new id`() = runTest {
        val id = (editor.add(24, "Kim") as WriteResult.Saved).value
        val added = names.observeNames(24).first().single { it.id == id }
        assertEquals(Name(id, groupId = 24, text = "Kim", selected = true, drawn = false), added)
    }

    @Test
    fun `add rejects a blank name and writes nothing`() = runTest {
        assertEquals(WriteResult.Blank, editor.add(24, "\t\n"))
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `rename stores the trimmed text`() = runTest {
        assertEquals(WriteResult.Saved(Unit), editor.rename(1, " Jack Bauer "))
        assertEquals("Jack Bauer", names.names.value.single { it.id == 1L }.text)
    }

    @Test
    fun `rename rejects a blank name and writes nothing`() = runTest {
        assertEquals(WriteResult.Blank, editor.rename(1, "\r\n"))
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `delete returns the deleted name`() = runTest {
        assertEquals(Name(1, groupId = 24, text = "Jack", selected = false), editor.delete(1))
        assertTrue(names.names.value.none { it.id == 1L })
    }

    @Test
    fun `setSelected turns one name on and off`() = runTest {
        editor.setSelected(1, true)
        assertTrue(names.names.value.single { it.id == 1L }.selected)
        editor.setSelected(1, false)
        assertEquals(false, names.names.value.single { it.id == 1L }.selected)
    }

    @Test
    fun `setSelected persists only that name`() = runTest {
        editor.setSelected(3, true)
        assertEquals(listOf("setSelected(3, true)"), names.calls)
    }

    @Test
    fun `selectAll selects every name in the group`() = runTest {
        editor.selectAll(24)
        assertTrue(names.observeNames(24).first().all { it.selected })
    }

    @Test
    fun `selectAll writes in bulk for the group only`() = runTest {
        editor.selectAll(24)
        assertEquals(listOf("setAllSelected(24, true)"), names.calls)
        assertEquals(false, names.observeNames(25).first().single().selected)
    }

    @Test
    fun `clearAll deselects every name in the group`() = runTest {
        editor.selectAll(24)
        editor.clearAll(24)
        assertTrue(names.observeNames(24).first().none { it.selected })
    }

    @Test
    fun `clearAll writes in bulk for the group only`() = runTest {
        names.names.value = names.names.value.map { it.copy(selected = true) }
        editor.clearAll(24)
        assertEquals(listOf("setAllSelected(24, false)"), names.calls)
        assertEquals(true, names.observeNames(25).first().single().selected)
    }

    @Test
    fun `select all and clear all leave drawn untouched`() = runTest {
        editor.selectAll(24)
        editor.clearAll(24)
        // Only the bulk selected write is used; NameRepository.setAllSelected must not change drawn.
        assertEquals(listOf("setAllSelected(24, true)", "setAllSelected(24, false)"), names.calls)
        assertEquals(listOf(false, true, false), names.observeNames(24).first().map { it.drawn })
    }
}
