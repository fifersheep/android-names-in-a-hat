package uk.lobsterdoodle.namepicker.ui.placeholder

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup

/**
 * Spec "Launch": opening Draw sets the active list; a list that is gone is reported, never opened.
 * The entry calls [DrawViewModel.onShown] only while it is started, i.e. still on the back stack.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DrawViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `an existing list is shown and becomes active once shown`() {
        val prefs = FakePreferences()
        val vm = DrawViewModel(3, FakeGroups(Group(3, "Year 5")), ActiveGroup(prefs))
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals(emptyList(), prefs.calls)
        vm.onShown()
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    @Test
    fun `being shown again does not reopen the list`() {
        val prefs = FakePreferences()
        val vm = DrawViewModel(3, FakeGroups(Group(3, "Year 5")), ActiveGroup(prefs))
        vm.onShown()
        vm.onShown()
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    @Test
    fun `a list still loading is not opened when shown`() {
        val main = StandardTestDispatcher()
        Dispatchers.setMain(main)
        val prefs = FakePreferences()
        val vm = DrawViewModel(3, FakeGroups(Group(3, "Year 5")), ActiveGroup(prefs))
        assertEquals(ListStatus.Loading, vm.status.value)
        vm.onShown()
        main.scheduler.advanceUntilIdle()
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals(emptyList(), prefs.calls)
    }

    @Test
    fun `a list that no longer exists is gone and never made active`() {
        val prefs = FakePreferences(activeGroupId = 3)
        val vm = DrawViewModel(3, FakeGroups(), ActiveGroup(prefs))
        vm.onShown()
        assertEquals(ListStatus.Gone, vm.status.value)
        assertEquals(emptyList(), prefs.calls)
    }

    @Test
    fun `a list deleted while open becomes gone`() = runTest {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = DrawViewModel(3, groups, ActiveGroup(FakePreferences()))
        groups.delete(3)
        assertEquals(ListStatus.Gone, vm.status.value)
    }

    @Test
    fun `a rename is shown and does not reopen the list`() = runTest {
        val prefs = FakePreferences()
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = DrawViewModel(3, groups, ActiveGroup(prefs))
        vm.onShown()
        groups.rename(3, "Year 6")
        vm.onShown()
        assertEquals(ListStatus.Present("Year 6"), vm.status.value)
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }
}
