package uk.lobsterdoodle.namepicker.ui.editlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.FakeGroups
import uk.lobsterdoodle.namepicker.FakeNames
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.usecase.NameEditor
import uk.lobsterdoodle.namepicker.ui.PendingDeletes

/** Edit list's names section: add, rename, delayed delete, validation and saved state (spec "Edit list", D12, D13, D15). */
@OptIn(ExperimentalCoroutinesApi::class)
class EditNamesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val groups = FakeGroups(Group(7, "Year 5"))
    private val names = FakeNames(
        Name(1, groupId = 7, text = "Zara", selected = true),
        Name(2, groupId = 7, text = "Ada", selected = false, drawn = true),
        Name(3, groupId = 7, text = "Tom", selected = true),
        Name(4, groupId = 8, text = "Other list", selected = true),
    )
    private val pendingDeletes = PendingDeletes()

    /** Stands in for the process-lifetime scope; unconfined, so writes run at once. */
    private var appScope = CoroutineScope(dispatcher)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(saved: SavedStateHandle = SavedStateHandle()) =
        EditNamesViewModel(7, names, groups, NameEditor(names), pendingDeletes, appScope, saved)

    /** A ViewModel in a store, so the test can clear it as leaving the screen does. */
    private fun stored(): Pair<EditNamesViewModel, ViewModelStore> {
        val store = ViewModelStore()
        val vm = ViewModelProvider.create(store, viewModelFactory { initializer { vm() } })[EditNamesViewModel::class]
        return vm to store
    }

    /** uiState is shared while subscribed, as the screen collects it. */
    private fun TestScope.collecting(vm: EditNamesViewModel): EditNamesViewModel {
        backgroundScope.launch { vm.uiState.collect {} }
        return vm
    }

    private fun EditNamesViewModel.shown() = uiState.value.names.map { it.text }

    // Listing

    @Test
    fun `names show in the order they were added and update live`() = runTest(dispatcher) {
        val vm = collecting(vm())
        assertTrue(vm.uiState.value.loaded)
        assertEquals(listOf("Zara", "Ada", "Tom"), vm.shown())
        names.add(7, "Aaron", selected = true)
        assertEquals(listOf("Zara", "Ada", "Tom", "Aaron"), vm.shown())
    }

    @Test
    fun `nothing is shown as loaded before the first read`() {
        val vm = vm()
        assertFalse(vm.uiState.value.loaded)
        assertEquals(emptyList(), vm.uiState.value.names)
    }

    // Add

    @Test
    fun `add stores the trimmed name selected and clears the field`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.onAddTextChange("  Ben  ")
        assertTrue(vm.add())
        assertEquals("", vm.addText)
        assertEquals(listOf("add(7, Ben, true)"), names.calls)
        assertEquals(listOf("Zara", "Ada", "Tom", "Ben"), vm.shown())
    }

    @Test
    fun `add puts inner line breaks and tabs on one line`() {
        val vm = vm()
        vm.onAddTextChange("Cy\nDee\tE")
        vm.add()
        assertEquals(listOf("add(7, Cy Dee E, true)"), names.calls)
    }

    @Test
    fun `add clears the field before the write runs`() = runTest {
        appScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val vm = vm()
        vm.onAddTextChange("Ben")
        vm.add()
        assertEquals("", vm.addText)
        assertEquals(emptyList(), names.calls)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("add(7, Ben, true)"), names.calls)
    }

    @Test
    fun `two quick adds insert the name once`() = runTest {
        appScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val vm = vm()
        vm.onAddTextChange("Ben")
        assertTrue(vm.add())
        assertFalse(vm.add())
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("add(7, Ben, true)"), names.calls)
    }

    @Test
    fun `a blank add shows the error and saves nothing`() {
        for (blank in listOf("", "   ", "\t\n ", " ")) {
            val vm = vm()
            vm.onAddTextChange(blank)
            assertFalse(vm.add())
            assertTrue(vm.addError, "error for <$blank>")
            assertEquals(blank, vm.addText)
        }
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `typing clears the add error`() {
        val vm = vm()
        vm.add()
        assertTrue(vm.addError)
        vm.onAddTextChange("B")
        assertFalse(vm.addError)
    }

    @Test
    fun `duplicates are allowed`() {
        val vm = vm()
        vm.onAddTextChange("Tom")
        assertTrue(vm.add())
        assertEquals(listOf("Zara", "Ada", "Tom", "Tom"), names.texts(7))
    }

    @Test
    fun `an add into a list deleted underneath does not crash`() {
        names.failWrites = true
        val vm = vm()
        vm.onAddTextChange("Ben")
        assertTrue(vm.add())
        assertEquals(listOf("Zara", "Ada", "Tom"), names.texts(7))
    }

    // Rename

    @Test
    fun `rename saves the trimmed name`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        assertEquals(3L, vm.renamingId)
        assertEquals("Tom", vm.renameText)
        vm.onRenameTextChange("  Tommy\n")
        assertTrue(vm.saveRename())
        assertNull(vm.renamingId)
        assertEquals(listOf("rename(3, Tommy)"), names.calls)
    }

    @Test
    fun `a blank rename shows the error and keeps the old name`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange(" \t")
        assertFalse(vm.saveRename())
        assertTrue(vm.renameError)
        assertEquals(3L, vm.renamingId)
        assertEquals(emptyList(), names.calls)
        vm.onRenameTextChange("T")
        assertFalse(vm.renameError)
    }

    @Test
    fun `an unchanged rename writes nothing`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange(" Tom ")
        assertTrue(vm.saveRename())
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `cancel closes the rename and writes nothing`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("Tommy")
        vm.cancelRename()
        assertNull(vm.renamingId)
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `renaming a blank name starts from an empty field`() {
        val vm = vm()
        vm.startRename(3, "  ")
        assertEquals("", vm.renameText)
    }

    @Test
    fun `opening another rename saves a valid changed draft`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("Tommy")
        vm.startRename(1, "Zara")
        assertEquals(1L, vm.renamingId)
        assertEquals("Zara", vm.renameText)
        assertEquals(listOf("rename(3, Tommy)"), names.calls)
    }

    @Test
    fun `opening another rename drops a blank draft`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("  ")
        vm.startRename(1, "Zara")
        assertEquals(1L, vm.renamingId)
        assertFalse(vm.renameError)
        assertEquals(emptyList(), names.calls)
    }

    // Delayed delete (D12)

    @Test
    fun `delete hides the name at once but leaves it stored`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Zara")
        assertEquals(listOf("Ada", "Tom"), vm.shown())
        assertEquals(PendingNameDelete(1, "Zara"), vm.uiState.value.pending)
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `undo shows the same name again`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Zara")
        assertEquals(PendingNameDelete(1, "Zara"), vm.undo(1))
        assertEquals(listOf("Zara", "Ada", "Tom"), vm.shown())
        assertEquals(1L, vm.uiState.value.names.first().id)
        assertNull(vm.uiState.value.pending)
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `the snackbar ending commits the delete`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Zara")
        vm.commit(1)
        assertEquals(listOf("delete(1)"), names.calls)
        assertEquals(listOf("Ada", "Tom"), vm.shown())
        assertNull(vm.uiState.value.pending)
    }

    @Test
    fun `a late result for an older delete changes nothing`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Zara")
        vm.delete(3, "Tom")
        assertNull(vm.undo(1))
        vm.commit(1)
        assertEquals(PendingNameDelete(3, "Tom"), vm.uiState.value.pending)
    }

    @Test
    fun `a second delete commits the first`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Zara")
        vm.delete(3, "Tom")
        assertEquals(listOf("delete(1)"), names.calls)
        assertEquals(listOf("Ada"), vm.shown())
    }

    @Test
    fun `deleting the name being renamed closes the rename`() {
        val vm = vm()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("Tommy")
        vm.delete(3, "Tom")
        assertNull(vm.renamingId)
        assertEquals(emptyList(), names.calls)
    }

    // Leaving the screen

    @Test
    fun `leaving commits the pending delete and a valid rename draft and drops the add text`() {
        val (vm, store) = stored()
        vm.delete(1, "Zara")
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("Tommy")
        vm.onAddTextChange("Half typed")
        store.clear()
        assertEquals(listOf("delete(1)", "rename(3, Tommy)"), names.calls)
        assertEquals(listOf("Ada", "Tommy"), names.texts(7))
    }

    @Test
    fun `leaving with a blank rename draft keeps the old name`() {
        val (vm, store) = stored()
        vm.startRename(3, "Tom")
        vm.onRenameTextChange(" ")
        store.clear()
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `leaving while the list is being deleted writes nothing`() {
        val (vm, store) = stored()
        vm.delete(1, "Zara")
        vm.startRename(3, "Tom")
        vm.onRenameTextChange("Tommy")
        pendingDeletes.add(7)
        store.clear()
        assertEquals(emptyList(), names.calls)
    }

    @Test
    fun `leaving after the list is gone writes nothing`() = runTest(dispatcher) {
        val (vm, store) = stored()
        vm.delete(1, "Zara")
        groups.delete(7)
        store.clear()
        assertEquals(emptyList(), names.calls)
    }

    // Rotation and process death: everything comes back from SavedStateHandle.

    @Test
    fun `typed text and an open rename survive recreation`() {
        val saved = SavedStateHandle()
        vm(saved).apply {
            onAddTextChange("Be")
            add()
            onAddTextChange("Ben draft")
            startRename(3, "Tom")
            onRenameTextChange("")
            saveRename()
        }
        val restored = vm(saved)
        assertEquals("Ben draft", restored.addText)
        assertEquals(3L, restored.renamingId)
        assertEquals("", restored.renameText)
        assertTrue(restored.renameError)
        restored.onRenameTextChange(" Tommy ")
        assertTrue(restored.saveRename())
        assertEquals(listOf("add(7, Be, true)", "rename(3, Tommy)"), names.calls)
    }

    @Test
    fun `the add error survives recreation`() {
        val saved = SavedStateHandle()
        vm(saved).add()
        assertTrue(vm(saved).addError)
    }

    @Test
    fun `a pending delete survives recreation and can still be undone`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        vm(saved).delete(1, "Zara")
        val restored = collecting(vm(saved))
        assertEquals(listOf("Ada", "Tom"), restored.shown())
        assertEquals(PendingNameDelete(1, "Zara"), restored.uiState.value.pending)
        restored.undo(1)
        assertEquals(listOf("Zara", "Ada", "Tom"), restored.shown())
        assertEquals(emptyList(), names.calls)
    }
}
