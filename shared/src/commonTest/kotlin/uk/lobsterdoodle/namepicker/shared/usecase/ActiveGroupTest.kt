package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import uk.lobsterdoodle.namepicker.shared.fakes.FakePreferencesRepository

class ActiveGroupTest {
    @Test
    fun `open stores the id`() = runTest {
        val prefs = FakePreferencesRepository()
        ActiveGroup(prefs).open(24)
        assertEquals(listOf("setActiveGroup(24)"), prefs.calls)
    }

    @Test
    fun `close clears it`() = runTest {
        val prefs = FakePreferencesRepository(activeGroupId = 24)
        ActiveGroup(prefs).close()
        assertEquals(listOf("clearActiveGroup()"), prefs.calls)
        assertNull(prefs.activeGroup.value)
    }

    @Test
    fun `observe is null when none`() = runTest {
        assertNull(ActiveGroup(FakePreferencesRepository()).observe().first())
    }

    @Test
    fun `observe returns the id`() = runTest {
        assertEquals(24L, ActiveGroup(FakePreferencesRepository(activeGroupId = 24)).observe().first())
    }
}
