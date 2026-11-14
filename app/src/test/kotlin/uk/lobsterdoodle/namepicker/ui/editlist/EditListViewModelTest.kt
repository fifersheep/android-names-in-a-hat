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
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.FakeGroups
import uk.lobsterdoodle.namepicker.FakePreferences
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.PendingDeletes

/** Edit list's list-name field: create, rename, validation and leaving (spec "Edit list", D15, rules 1–3). */
@OptIn(ExperimentalCoroutinesApi::class)
class EditListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val pendingDeletes = PendingDeletes()
    private val appScope = CoroutineScope(dispatcher)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(listId: Long?, groups: FakeGroups, saved: SavedStateHandle = SavedStateHandle()) =
        EditListViewModel(listId, groups, GroupEditor(groups, FakePreferences()), pendingDeletes, appScope, saved)

    /** A ViewModel in a store, so the test can clear it as leaving the screen does. */
    private fun stored(listId: Long?, groups: FakeGroups): Pair<EditListViewModel, ViewModelStore> {
        val store = ViewModelStore()
        val vm = ViewModelProvider.create(store, viewModelFactory { initializer { vm(listId, groups) } })[EditListViewModel::class]
        return vm to store
    }

    private fun EditListViewModel.commit(): List<Long> = mutableListOf<Long>().also { created -> commitName { created += it } }

    private fun FakeGroups.labels() = groups.value.map { it.label }

    // New list

    @Test
    fun `a new list starts unsaved and empty`() {
        val vm = vm(null, FakeGroups())
        assertEquals(ListStatus.Unsaved, vm.status.value)
        assertEquals("", vm.text)
        assertFalse(vm.error)
        assertFalse(vm.listExists)
    }

    @Test
    fun `committing a new list saves the trimmed name once and reports its id`() {
        val groups = FakeGroups()
        val vm = vm(null, groups)
        vm.onTextChange("  Team  ")
        assertEquals(listOf(1L), vm.commit())
        assertEquals(listOf(Group(1, "Team")), groups.groups.value)
        assertEquals(ListStatus.Present("Team"), vm.status.value)
        assertEquals("Team", vm.text)
        assertTrue(vm.listExists)
    }

    @Test
    fun `committing again renames instead of creating a second list`() {
        val groups = FakeGroups()
        val vm = vm(null, groups)
        vm.onTextChange("Year 5")
        vm.commit()
        vm.onTextChange("Year 6")
        assertEquals(emptyList(), vm.commit())
        assertEquals(listOf(Group(1, "Year 6")), groups.groups.value)
    }

    @Test
    fun `a blank name shows the error and creates nothing`() {
        for (blank in listOf("", "   ", "\t\n ")) {
            val groups = FakeGroups()
            val vm = vm(null, groups)
            vm.onTextChange(blank)
            assertEquals(emptyList(), vm.commit())
            assertTrue(vm.error, "error for <$blank>")
            assertEquals(emptyList(), groups.groups.value)
            assertEquals(ListStatus.Unsaved, vm.status.value)
        }
    }

    @Test
    fun `typing clears the error`() {
        val vm = vm(null, FakeGroups())
        vm.commit()
        assertTrue(vm.error)
        vm.onTextChange("T")
        assertFalse(vm.error)
    }

    @Test
    fun `done on a list created in this visit finishes`() {
        val vm = vm(null, FakeGroups())
        vm.onTextChange("Team")
        vm.commit()
        var done = 0
        vm.done { done++ }
        assertEquals(1, done)
    }

    @Test
    fun `done before the list exists does nothing`() {
        val vm = vm(null, FakeGroups())
        vm.onTextChange("Team")
        var done = 0
        vm.done { done++ }
        assertEquals(0, done)
    }

    @Test
    fun `leaving a new list before committing creates nothing`() {
        val groups = FakeGroups()
        val (vm, store) = stored(null, groups)
        vm.onTextChange("Half typed")
        store.clear()
        assertEquals(emptyList(), groups.groups.value)
    }

    // Existing list

    @Test
    fun `an existing list is prefilled with its name`() {
        val vm = vm(3, FakeGroups(Group(3, "Year 5")))
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals("Year 5", vm.text)
        assertTrue(vm.listExists)
    }

    @Test
    fun `committing an existing list renames it trimmed`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        vm.onTextChange("  Year 6 ")
        assertEquals(emptyList(), vm.commit())
        assertEquals(listOf("Year 6"), groups.labels())
        assertEquals("Year 6", vm.text)
    }

    @Test
    fun `a blank rename shows the error and keeps the old name`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        vm.onTextChange("  ")
        vm.commit()
        assertTrue(vm.error)
        assertEquals(listOf("Year 5"), groups.labels())
    }

    @Test
    fun `done renames a changed name then finishes`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        vm.onTextChange("Year 6")
        var done = 0
        vm.done { done++ }
        assertEquals(1, done)
        assertEquals(listOf("Year 6"), groups.labels())
    }

    @Test
    fun `done with a blank name shows the error and stays`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        vm.onTextChange("")
        var done = 0
        vm.done { done++ }
        assertEquals(0, done)
        assertTrue(vm.error)
        assertEquals(listOf("Year 5"), groups.labels())
    }

    @Test
    fun `done keeps the rename when the screen is cleared before the write runs`() {
        // Main queues work, so viewModelScope hasn't run anything when the store is cleared.
        Dispatchers.setMain(StandardTestDispatcher())
        val groups = FakeGroups(Group(3, "Year 5"))
        val (vm, store) = stored(3, groups)
        vm.onTextChange("Year 6")
        vm.done {}
        store.clear()
        assertEquals(listOf("Year 6"), groups.labels())
    }

    @Test
    fun `leaving an existing list saves a changed name`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val (vm, store) = stored(3, groups)
        vm.onTextChange(" Year 6 ")
        store.clear()
        assertEquals(listOf("Year 6"), groups.labels())
    }

    @Test
    fun `leaving an existing list with a blank name keeps the old one`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val (vm, store) = stored(3, groups)
        vm.onTextChange("   ")
        store.clear()
        assertEquals(listOf("Year 5"), groups.labels())
    }

    @Test
    fun `leaving a list that is being deleted does not rename it`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val (vm, store) = stored(3, groups)
        vm.onTextChange("Half typed")
        pendingDeletes.add(3)
        store.clear()
        assertEquals(listOf("Year 5"), groups.labels())
    }

    @Test
    fun `an existing list deleted while open becomes gone`() = runTest(dispatcher) {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        groups.delete(3)
        assertEquals(ListStatus.Gone, vm.status.value)
    }

    // Rotation and process death: everything comes back from SavedStateHandle.

    @Test
    fun `typed text and the error survive recreation`() {
        val groups = FakeGroups(Group(3, "Year 5"))
        val saved = SavedStateHandle()
        vm(3, groups, saved).apply {
            onTextChange("Year 6 draft")
            onTextChange("")
            commit()
        }
        val restored = vm(3, groups, saved)
        assertEquals("", restored.text)
        assertTrue(restored.error)

        vm(3, groups, saved).onTextChange("Year 6 draft")
        assertEquals("Year 6 draft", vm(3, groups, saved).text)
    }

    @Test
    fun `a list created before recreation is not created again`() {
        val groups = FakeGroups()
        val saved = SavedStateHandle()
        vm(null, groups, saved).apply {
            onTextChange("Team")
            commit()
        }
        // The shell's key still says null if it was saved before the swap; the ViewModel knows better.
        val restored = vm(null, groups, saved)
        assertTrue(restored.listExists)
        restored.onTextChange("Team 2")
        assertEquals(emptyList(), restored.commit())
        assertEquals(listOf(Group(1, "Team 2")), groups.groups.value)
    }
}
