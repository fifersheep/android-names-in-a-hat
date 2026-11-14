package uk.lobsterdoodle.namepicker

import androidx.navigation3.runtime.NavKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import uk.lobsterdoodle.namepicker.navigation.Draw
import uk.lobsterdoodle.namepicker.navigation.Lists
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup

/** Spec "Launch": route from the stored active list; clear it only on a back-out or when gone. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShellViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun shell(prefs: FakePreferences) = ShellViewModel(prefs, ActiveGroup(prefs))

    @Test
    fun `a stored active list launches into Draw over Lists`() {
        assertEquals<List<NavKey>?>(listOf(Lists, Draw(7)), shell(FakePreferences(activeGroupId = 7)).start.value)
    }

    @Test
    fun `no active list launches into Lists`() {
        assertEquals<List<NavKey>?>(listOf(Lists), shell(FakePreferences()).start.value)
    }

    @Test
    fun `an unreadable settings file launches into Lists`() {
        assertEquals<List<NavKey>?>(listOf(Lists), shell(FakePreferences(failReads = true)).start.value)
    }

    @Test
    fun `the route is unknown until the settings are read`() {
        Dispatchers.setMain(kotlinx.coroutines.test.StandardTestDispatcher())
        assertNull(shell(FakePreferences(activeGroupId = 7)).start.value)
    }

    @Test
    fun `leaving a list clears the active list`() {
        val prefs = FakePreferences(activeGroupId = 7)
        shell(prefs).onLeftList()
        assertEquals(listOf("clearActiveGroup()"), prefs.calls)
        assertNull(prefs.active.value)
    }

    @Test
    fun `a gone list is cleared only if it is still the active one`() {
        val prefs = FakePreferences(activeGroupId = 7)
        val shell = shell(prefs)
        shell.onListGone(3)
        assertEquals(7L, prefs.active.value)
        shell.onListGone(7)
        assertEquals(listOf("clearActiveGroupIf(3)", "clearActiveGroupIf(7)"), prefs.calls)
        assertNull(prefs.active.value)
    }
}
