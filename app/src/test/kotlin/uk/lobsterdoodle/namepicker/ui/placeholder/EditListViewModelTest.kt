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
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor

/** The Edit list placeholder: create once, then behave like an existing list (spec rule 1). */
@OptIn(ExperimentalCoroutinesApi::class)
class EditListViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(listId: Long?, groups: FakeGroups) =
        EditListViewModel(listId, groups, GroupEditor(groups, FakePreferences()))

    @Test
    fun `a new list is unsaved`() {
        assertEquals(ListStatus.Unsaved, vm(null, FakeGroups()).status.value)
    }

    @Test
    fun `creating saves the trimmed name and reports the new id`() {
        val groups = FakeGroups()
        val vm = vm(null, groups)
        val created = mutableListOf<Long>()
        vm.create("  Year 5 ") { created += it }
        assertEquals(listOf(1L), created)
        assertEquals(listOf(Group(1, "Year 5")), groups.groups.value)
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
    }

    @Test
    fun `a blank name creates nothing`() {
        val groups = FakeGroups()
        val vm = vm(null, groups)
        val created = mutableListOf<Long>()
        vm.create("   ") { created += it }
        assertEquals(emptyList(), created)
        assertEquals(emptyList(), groups.groups.value)
        assertEquals(ListStatus.Unsaved, vm.status.value)
    }

    @Test
    fun `creating twice creates one list`() {
        val groups = FakeGroups()
        val vm = vm(null, groups)
        vm.create("Year 5") {}
        vm.create("Year 6") {}
        assertEquals(1, groups.groups.value.size)
    }

    @Test
    fun `an existing list is shown`() {
        assertEquals(ListStatus.Present("Year 5"), vm(3, FakeGroups(Group(3, "Year 5"))).status.value)
    }

    @Test
    fun `an existing list deleted while open becomes gone`() = runTest {
        val groups = FakeGroups(Group(3, "Year 5"))
        val vm = vm(3, groups)
        groups.delete(3)
        assertEquals(ListStatus.Gone, vm.status.value)
    }
}
