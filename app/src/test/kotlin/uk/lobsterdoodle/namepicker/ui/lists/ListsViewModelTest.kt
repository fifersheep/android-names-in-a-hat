package uk.lobsterdoodle.namepicker.ui.lists

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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.FakeGroups
import uk.lobsterdoodle.namepicker.FakePreferences
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor
import uk.lobsterdoodle.namepicker.ui.PendingDeletes

/** The Lists screen's state and its delayed delete (spec "Lists", "Delayed delete" D12). */
@OptIn(ExperimentalCoroutinesApi::class)
class ListsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val groups = FakeGroups(Group(1, "Year 5 Maple"), Group(2, "Year 6 Oak"))
    private val prefs = FakePreferences(activeGroupId = 1)
    private val pendingDeletes = PendingDeletes()

    /** Stands in for the process-lifetime scope; unconfined, so commits run at once. */
    private val appScope = CoroutineScope(dispatcher)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(saved: SavedStateHandle = SavedStateHandle()) =
        ListsViewModel(groups, GroupEditor(groups, prefs), pendingDeletes, appScope, saved)

    /** uiState is shared while subscribed, as the screen collects it. */
    private fun TestScope.collecting(vm: ListsViewModel): ListsViewModel {
        backgroundScope.launch { vm.uiState.collect {} }
        return vm
    }

    private fun ListsViewModel.ids() = uiState.value.lists.map { it.id }

    @Test
    fun `lists come newest first with name and drawn counts`() = runTest(dispatcher) {
        groups.counts[1] = 24 to 4
        val vm = collecting(vm())
        assertTrue(vm.uiState.value.loaded)
        assertEquals(
            listOf(ListCard(2, "Year 6 Oak", 0, 0), ListCard(1, "Year 5 Maple", 24, 4)),
            vm.uiState.value.lists,
        )
    }

    @Test
    fun `lists update live`() = runTest(dispatcher) {
        val vm = collecting(vm())
        groups.create("Year 7")
        groups.rename(2, "Year 6 Ash")
        assertEquals(listOf("Year 7", "Year 6 Ash", "Year 5 Maple"), vm.uiState.value.lists.map { it.label })
    }

    @Test
    fun `delete hides the list at once but leaves it stored`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Year 5 Maple", wasOpen = false)
        assertEquals(listOf(2L), vm.ids())
        assertEquals(PendingDelete(1, "Year 5 Maple", reopen = false), vm.uiState.value.pending)
        assertEquals(2, groups.groups.value.size)
        assertTrue(1L in pendingDeletes)
        assertEquals(emptyList(), prefs.calls)
    }

    @Test
    fun `undo brings the list back and says whether to reopen it`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Year 5 Maple", wasOpen = true)
        assertEquals(PendingDelete(1, "Year 5 Maple", reopen = true), vm.undo(1))
        assertEquals(listOf(2L, 1L), vm.ids())
        assertNull(vm.uiState.value.pending)
        assertFalse(1L in pendingDeletes)
        assertEquals(2, groups.groups.value.size)
    }

    @Test
    fun `commit deletes the list and its settings and keeps it hidden`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Year 5 Maple", wasOpen = false)
        vm.commit(1)
        assertEquals(listOf(Group(2, "Year 6 Oak")), groups.groups.value)
        assertEquals(listOf("removeSortType(1)", "clearActiveGroupIf(1)"), prefs.calls)
        assertEquals(listOf(2L), vm.ids())
        assertNull(vm.uiState.value.pending)
        assertFalse(1L in pendingDeletes)
    }

    @Test
    fun `commit and undo for a list that is no longer pending do nothing`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Year 5 Maple", wasOpen = false)
        vm.commit(2)
        assertNull(vm.undo(2))
        assertEquals(PendingDelete(1, "Year 5 Maple", reopen = false), vm.uiState.value.pending)
        assertEquals(2, groups.groups.value.size)
    }

    @Test
    fun `a second delete commits the first`() = runTest(dispatcher) {
        val vm = collecting(vm())
        vm.delete(1, "Year 5 Maple", wasOpen = false)
        vm.delete(2, "Year 6 Oak", wasOpen = false)
        assertEquals(listOf(Group(2, "Year 6 Oak")), groups.groups.value)
        assertEquals(PendingDelete(2, "Year 6 Oak", reopen = false), vm.uiState.value.pending)
        assertEquals(emptyList(), vm.ids())
    }

    @Test
    fun `commitPending with nothing pending does nothing`() = runTest(dispatcher) {
        vm().commitPending()
        assertEquals(2, groups.groups.value.size)
    }

    @Test
    fun `clearing the ViewModel commits a pending delete`() = runTest(dispatcher) {
        val store = ViewModelStore()
        val vm = ViewModelProvider.create(store, viewModelFactory { initializer { vm() } })[ListsViewModel::class]
        vm.delete(1, "Year 5 Maple", wasOpen = false)
        store.clear()
        assertEquals(listOf(Group(2, "Year 6 Oak")), groups.groups.value)
    }

    @Test
    fun `a pending delete survives the ViewModel being recreated`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        vm(saved).delete(1, "Year 5 Maple", wasOpen = true)

        val restoredDeletes = PendingDeletes()
        val restored = collecting(ListsViewModel(groups, GroupEditor(groups, prefs), restoredDeletes, appScope, saved))
        assertEquals(PendingDelete(1, "Year 5 Maple", reopen = true), restored.uiState.value.pending)
        assertEquals(listOf(2L), restored.ids())
        assertTrue(1L in restoredDeletes)
        assertEquals(2, groups.groups.value.size)
    }
}
