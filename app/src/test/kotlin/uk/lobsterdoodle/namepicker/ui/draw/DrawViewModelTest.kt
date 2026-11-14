package uk.lobsterdoodle.namepicker.ui.draw

import androidx.lifecycle.SavedStateHandle
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.FakeGroups
import uk.lobsterdoodle.namepicker.FakeNames
import uk.lobsterdoodle.namepicker.FakePreferences
import uk.lobsterdoodle.namepicker.shared.draw.DrawBarState
import uk.lobsterdoodle.namepicker.shared.draw.NameDraw
import uk.lobsterdoodle.namepicker.shared.draw.RandomNumberGenerator
import uk.lobsterdoodle.namepicker.shared.draw.SelectAllBox
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup
import uk.lobsterdoodle.namepicker.shared.usecase.NameEditor
import uk.lobsterdoodle.namepicker.shared.usecase.SortOrder
import uk.lobsterdoodle.namepicker.ui.ListStatus

/**
 * The Draw screen's ViewModel (NAM-10 AC13): selection, select all, sort, count clamping, draw and
 * the saved-state result. Spec "Launch": opening Draw sets the active list; a list that is gone is
 * reported, never opened. The entry calls [DrawViewModel.onShown] only while it is started.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DrawViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val groups = FakeGroups(Group(3, "Year 5"))
    private val prefs = FakePreferences()

    private fun name(id: Long, text: String, selected: Boolean = true, drawn: Boolean = false) =
        Name(id, groupId = 3, text = text, selected = selected, drawn = drawn)

    private val six = FakeNames(
        name(1, "zara"), name(2, "Ada"), name(3, "Tom"),
        name(4, "ben", selected = false), name(5, "Kai"), name(6, "Chloe", selected = false),
    )

    private fun vm(
        names: FakeNames = six,
        saved: SavedStateHandle = SavedStateHandle(),
        groups: FakeGroups = this.groups,
        prefs: FakePreferences = this.prefs,
        seed: Int = 7,
    ) = DrawViewModel(
        listId = 3,
        groups = groups,
        activeGroup = ActiveGroup(prefs),
        names = names,
        editor = NameEditor(names),
        sortOrder = SortOrder(prefs),
        nameDraw = NameDraw(RandomNumberGenerator(Random(seed))),
        saved = saved,
    )

    private val DrawViewModel.state get() = uiState.value

    // --- List status and the active list (NAM-7) ---

    @Test
    fun `an existing list is shown and becomes active once shown`() {
        val vm = vm()
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals(emptyList(), prefs.calls)
        vm.onShown()
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    @Test
    fun `being shown again does not reopen the list`() {
        val vm = vm()
        vm.onShown()
        vm.onShown()
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    @Test
    fun `a list still loading is not opened when shown`() {
        val main = StandardTestDispatcher()
        Dispatchers.setMain(main)
        val vm = vm()
        assertEquals(ListStatus.Loading, vm.status.value)
        vm.onShown()
        main.scheduler.advanceUntilIdle()
        assertEquals(ListStatus.Present("Year 5"), vm.status.value)
        assertEquals(emptyList(), prefs.calls)
    }

    @Test
    fun `a list that no longer exists is gone and never made active`() {
        val prefs = FakePreferences(activeGroupId = 3)
        val vm = vm(groups = FakeGroups(), prefs = prefs)
        vm.onShown()
        assertEquals(ListStatus.Gone, vm.status.value)
        assertEquals(emptyList(), prefs.calls)
    }

    @Test
    fun `a list deleted while open becomes gone`() = runTest {
        val vm = vm()
        groups.delete(3)
        assertEquals(ListStatus.Gone, vm.status.value)
    }

    @Test
    fun `a rename is shown and does not reopen the list`() = runTest {
        val vm = vm()
        vm.onShown()
        groups.rename(3, "Year 6")
        vm.onShown()
        assertEquals(ListStatus.Present("Year 6"), vm.status.value)
        assertEquals(listOf("setActiveGroup(3)"), prefs.calls)
    }

    // --- Loading and the names ---

    @Test
    fun `names show with their stored selection keyed by id`() {
        val state = vm().state
        assertTrue(state.loaded)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), state.chips.map { it.id })
        assertEquals(
            listOf(ChipVisual.IN, ChipVisual.IN, ChipVisual.IN, ChipVisual.OUT, ChipVisual.IN, ChipVisual.OUT),
            state.chips.map { it.visual },
        )
        assertEquals(4, state.draw.inHat)
        assertEquals(6, state.draw.total)
    }

    @Test
    fun `drawn names show as drawn whatever their selection`() {
        val names = FakeNames(name(1, "Ada", drawn = true), name(2, "Ben", selected = false, drawn = true), name(3, "Cy"))
        val state = vm(names).state
        assertEquals(listOf(ChipVisual.DRAWN, ChipVisual.DRAWN, ChipVisual.IN), state.chips.map { it.visual })
        assertEquals(2, state.draw.drawnCount)
        assertEquals(1, state.draw.inHat)
    }

    @Test
    fun `nothing is loaded until the names arrive`() {
        val main = StandardTestDispatcher()
        Dispatchers.setMain(main)
        val vm = vm()
        assertFalse(vm.state.loaded)
        main.scheduler.advanceUntilIdle()
        assertTrue(vm.state.loaded)
    }

    @Test
    fun `a list with no names loads as empty`() {
        val state = vm(FakeNames()).state
        assertTrue(state.loaded)
        assertFalse(state.draw.hasNames)
    }

    // --- Selection ---

    @Test
    fun `toggling a name saves the opposite selection`() = runTest {
        val vm = vm()
        vm.toggle(1)
        vm.toggle(4)
        assertEquals(listOf(false, true), six.names.value.filter { it.id in setOf(1L, 4L) }.map { it.selected })
        assertEquals(ChipVisual.OUT, vm.state.chips.first { it.id == 1L }.visual)
    }

    @Test
    fun `a drawn name is not toggled`() = runTest {
        val names = FakeNames(name(1, "Ada", drawn = true))
        vm(names).toggle(1)
        assertTrue(names.names.value.single().selected)
    }

    @Test
    fun `two quick toggles of one name end where they started`() = runTest {
        val gate = CompletableDeferred<Unit>()
        six.selectionGate = gate
        val vm = vm()
        vm.toggle(1)
        vm.toggle(1)
        gate.complete(Unit)
        assertTrue(six.names.value.first { it.id == 1L }.selected)
    }

    @Test
    fun `select all selects every name when some are out`() = runTest {
        val vm = vm()
        assertEquals(SelectAllBox.SOME, vm.state.draw.box)
        vm.toggleAll()
        assertTrue(six.names.value.all { it.selected })
        assertEquals(SelectAllBox.ALL, vm.state.draw.box)
    }

    @Test
    fun `select all clears every name when all are in`() = runTest {
        val names = FakeNames(name(1, "Ada"), name(2, "Ben"))
        val vm = vm(names)
        vm.toggleAll()
        assertTrue(names.names.value.none { it.selected })
        assertEquals(SelectAllBox.NONE, vm.state.draw.box)
    }

    @Test
    fun `select all never changes drawn state`() = runTest {
        val names = FakeNames(name(1, "Ada", selected = false, drawn = true), name(2, "Ben", selected = false))
        vm(names).toggleAll()
        assertEquals(listOf(true, false), names.names.value.map { it.drawn })
        assertTrue(names.names.value.all { it.selected })
        assertFalse(names.calls.any { it.startsWith("markDrawn") || it.startsWith("clearDrawn") })
    }

    @Test
    fun `select all does nothing when every name is drawn`() = runTest {
        val names = FakeNames(name(1, "Ada", selected = false, drawn = true))
        val vm = vm(names)
        assertEquals(SelectAllBox.DISABLED, vm.state.draw.box)
        vm.toggleAll()
        assertFalse(names.names.value.single().selected)
    }

    // --- Sort ---

    @Test
    fun `sort cycles unsorted to A-Z to Z-A to unsorted and orders the chips ignoring case`() = runTest {
        val vm = vm()
        assertEquals(NameSortType.NONE, vm.state.sort)
        assertEquals(listOf("zara", "Ada", "Tom", "ben", "Kai", "Chloe"), vm.state.chips.map { it.text })
        vm.cycleSort()
        assertEquals(NameSortType.ASC, vm.state.sort)
        assertEquals(listOf("Ada", "ben", "Chloe", "Kai", "Tom", "zara"), vm.state.chips.map { it.text })
        vm.cycleSort()
        assertEquals(NameSortType.DESC, vm.state.sort)
        assertEquals(listOf("zara", "Tom", "Kai", "Chloe", "ben", "Ada"), vm.state.chips.map { it.text })
        vm.cycleSort()
        assertEquals(NameSortType.NONE, vm.state.sort)
        assertEquals(NameSortType.NONE, prefs.sorts.value[3L])
    }

    @Test
    fun `the stored sort order is used on open`() {
        val prefs = FakePreferences(sortTypes = mapOf(3L to NameSortType.DESC))
        val vm = vm(prefs = prefs)
        assertEquals(NameSortType.DESC, vm.state.sort)
        assertEquals("zara", vm.state.chips.first().text)
    }

    // --- Count ---

    @Test
    fun `the count starts at one`() {
        val state = vm().state
        assertEquals(1, state.count)
        assertEquals(DrawBarState.Stepper(max = 4), state.draw.bar)
    }

    @Test
    fun `the count steps within one to the number in the hat`() {
        val vm = vm()
        vm.decrement()
        assertEquals(1, vm.state.count)
        repeat(6) { vm.increment() }
        assertEquals(4, vm.state.count)
    }

    @Test
    fun `deselecting below the count clamps it and it stays clamped when names return`() = runTest {
        val vm = vm()
        repeat(3) { vm.increment() }
        assertEquals(4, vm.state.count)
        vm.toggle(1)
        vm.toggle(2)
        assertEquals(2, vm.state.count)
        vm.toggle(1)
        vm.toggle(2)
        assertEquals(2, vm.state.count)
    }

    @Test
    fun `the count rests at one when the hat empties`() = runTest {
        val vm = vm()
        repeat(2) { vm.increment() }
        vm.toggleAll()
        vm.toggleAll()
        assertEquals(DrawBarState.NothingInHat, vm.state.draw.bar)
        assertEquals(1, vm.state.count)
    }

    @Test
    fun `a restored ViewModel keeps the stored count`() {
        val saved = SavedStateHandle()
        val first = vm(saved = saved)
        repeat(2) { first.increment() }
        val restored = vm(saved = saved)
        assertEquals(3, restored.state.count)
    }

    @Test
    fun `the stored count is not clamped before the names load`() {
        val main = StandardTestDispatcher()
        Dispatchers.setMain(main)
        val saved = SavedStateHandle(mapOf(DrawViewModel.KEY_COUNT to 3))
        val vm = vm(saved = saved)
        main.scheduler.advanceUntilIdle()
        assertEquals(3, vm.state.count)
        assertEquals(3, saved.get<Int>(DrawViewModel.KEY_COUNT))
    }

    // --- Draw ---

    @Test
    fun `a draw of k returns k distinct names from the hat`() = runTest {
        val vm = vm()
        repeat(2) { vm.increment() }
        vm.draw()
        val reveal = assertNotNull(vm.state.reveal)
        assertEquals(3, reveal.names.size)
        assertEquals(3, reveal.names.map { it.id }.toSet().size)
        val inHat = six.names.value.filter { it.selected && !it.drawn }.map { it.id }.toSet()
        assertTrue(reveal.names.all { it.id in inHat })
    }

    @Test
    fun `a seeded draw is deterministic`() = runTest {
        val a = vm(seed = 42).apply { increment(); draw() }.state.reveal
        val b = vm(seed = 42).apply { increment(); draw() }.state.reveal
        assertEquals(a, b)
    }

    @Test
    fun `a draw never marks names drawn`() = runTest {
        vm().draw()
        assertEquals(emptyList(), six.calls)
        assertTrue(six.names.value.none { it.drawn })
    }

    @Test
    fun `a name toggled off just before the draw is never drawn`() = runTest {
        val names = FakeNames(name(1, "Ada"), name(2, "Ben"))
        val gate = CompletableDeferred<Unit>()
        names.selectionGate = gate
        val vm = vm(names)
        vm.increment()
        vm.toggle(1)
        vm.draw()
        assertNull(vm.state.reveal)
        gate.complete(Unit)
        assertEquals(listOf(2L), vm.state.reveal?.names?.map { it.id })
    }

    @Test
    fun `a double tap draws once`() = runTest {
        val gate = CompletableDeferred<Unit>()
        six.selectionGate = gate
        val vm = vm()
        vm.toggle(4)
        vm.draw()
        vm.draw()
        gate.complete(Unit)
        val first = vm.state.reveal
        assertNotNull(first)
        vm.draw()
        assertEquals(first, vm.state.reveal)
    }

    @Test
    fun `there is no draw with nothing in the hat`() = runTest {
        val names = FakeNames(name(1, "Ada", selected = false))
        val vm = vm(names)
        vm.draw()
        assertNull(vm.state.reveal)
    }

    @Test
    fun `the result survives a restore from saved state`() = runTest {
        val saved = SavedStateHandle()
        val first = vm(saved = saved)
        first.increment()
        first.draw()
        first.onRevealPlayed()
        val restored = vm(saved = saved, seed = 99)
        assertEquals(first.state.reveal, restored.state.reveal)
        assertTrue(assertNotNull(restored.state.reveal).played)
    }

    @Test
    fun `the result keeps the drawn text after a rename`() = runTest {
        val names = FakeNames(name(1, "Ada"))
        val vm = vm(names)
        vm.draw()
        names.rename(1, "Ada L")
        assertEquals(listOf(RevealName(1, "Ada")), vm.state.reveal?.names)
    }

    @Test
    fun `the reveal offers the count again or as many as are left`() = runTest {
        val vm = vm()
        repeat(2) { vm.increment() }
        vm.draw()
        val reveal = assertNotNull(vm.state.reveal)
        assertEquals(3, reveal.more)
        assertFalse(reveal.noneLeft)
    }

    @Test
    fun `draw more replaces the result and replays`() = runTest {
        val vm = vm(seed = 3)
        vm.draw()
        vm.onRevealPlayed()
        assertTrue(assertNotNull(vm.state.reveal).played)
        vm.drawMore()
        val after = assertNotNull(vm.state.reveal)
        assertFalse(after.played)
        assertEquals(1, after.names.size)
    }

    @Test
    fun `dismiss clears the result`() = runTest {
        val saved = SavedStateHandle()
        val vm = vm(saved = saved)
        vm.draw()
        vm.dismiss()
        assertNull(vm.state.reveal)
        assertNull(vm(saved = saved).state.reveal)
    }
}
