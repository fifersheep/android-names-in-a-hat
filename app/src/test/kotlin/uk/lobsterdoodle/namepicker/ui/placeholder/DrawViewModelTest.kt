package uk.lobsterdoodle.namepicker.ui.placeholder

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.FakeGroups
import uk.lobsterdoodle.namepicker.FakePreferences
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup

/** Spec "Launch": opening Draw sets the active list; a list that is gone is reported, never opened. */
@OptIn(ExperimentalCoroutinesApi::class)
class DrawViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `an existing list is shown and becomes the active list`() {
        val prefs = FakePreferences()
        val vm = DrawViewModel(3, FakeGroups(Group(3, "Year 5")), ActiveGroup(prefs))
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    @Test
    fun `a list that no longer exists is gone and never made active`() {
        val prefs = FakePreferences(activeGroupId = 3)
        val vm = DrawViewModel(3, FakeGroups(), ActiveGroup(prefs))
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
        groups.rename(3, "Year 6")
        assertEquals(ListStatus.Present("Year 6"), vm.status.value)
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }
}
