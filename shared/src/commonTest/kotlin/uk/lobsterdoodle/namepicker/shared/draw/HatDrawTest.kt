package uk.lobsterdoodle.namepicker.shared.draw

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import uk.lobsterdoodle.namepicker.shared.fakes.FakeNameRepository
import uk.lobsterdoodle.namepicker.shared.model.Name

/** Draw from remaining (NAM-12; spec "Draw from remaining"): draws mark names drawn until reset or put back. */
class HatDrawTest {
    private val g = 1L

    private fun name(id: Long, selected: Boolean = true, drawn: Boolean = false) =
        Name(id, g, "N$id", selected, drawn)

    private fun sixSelected() = FakeNameRepository((1L..6L).map { name(it) })

    private fun hat(repo: FakeNameRepository, seed: Int = 1) = HatDraw(repo, NameDraw(RandomNumberGenerator(Random(seed))))

    private suspend fun FakeNameRepository.all() = observeNames(g).first()

    @Test
    fun `a second draw never repeats a name from the first`() = runTest {
        // AC1, over many seeds.
        for (seed in 0 until 50) {
            val repo = sixSelected()
            val hat = hat(repo, seed)
            val first = hat.draw(g, 2).map { it.id }.toSet()
            val second = hat.draw(g, 2).map { it.id }.toSet()
            assertEquals(2, first.size, "seed $seed")
            assertEquals(2, second.size, "seed $seed")
            assertTrue(first.intersect(second).isEmpty(), "seed $seed: $first then $second")
        }
    }

    @Test
    fun `a draw marks exactly the drawn names and leaves selection alone`() = runTest {
        val repo = FakeNameRepository(listOf(name(1), name(2), name(3, selected = false)))
        val picked = hat(repo).draw(g, 1)
        assertEquals(listOf("markDrawn(${picked.map { it.id }})"), repo.calls)
        assertEquals(setOf(picked.single().id), repo.all().filter { it.drawn }.map { it.id }.toSet())
        assertEquals(listOf(true, true, false), repo.all().map { it.selected })
    }

    @Test
    fun `drawing every selected name leaves the hat empty with everyone drawn`() = runTest {
        // AC2.
        val repo = FakeNameRepository(listOf(name(1), name(2), name(3, selected = false)))
        val hat = hat(repo)
        hat.draw(g, 2)
        val state = DrawState.of(repo.all())
        assertEquals(0, state.inHat)
        assertEquals(DrawBarState.AllDrawn, state.bar)
        assertEquals(emptyList(), hat.draw(g, 1))
    }

    @Test
    fun `the last name in the hat can be drawn`() = runTest {
        // AC6.
        val repo = FakeNameRepository(listOf(name(1, drawn = true), name(2), name(3, selected = false)))
        assertEquals(listOf(2L), hat(repo).draw(g, 1).map { it.id })
    }

    @Test
    fun `a failed mark returns no result`() = runTest {
        val repo = sixSelected().apply { failMarkDrawn = true }
        assertFailsWith<IllegalStateException> { hat(repo).draw(g, 2) }
        assertTrue(repo.all().none { it.drawn })
    }

    @Test
    fun `reset clears drawn and keeps the selection`() = runTest {
        // AC3.
        val repo = FakeNameRepository(listOf(name(1, drawn = true), name(2, selected = false, drawn = true), name(3)))
        val snapshot = hat(repo).reset(g)
        assertEquals(listOf(1L, 2L), snapshot)
        assertTrue(repo.all().none { it.drawn })
        assertEquals(listOf(true, false, true), repo.all().map { it.selected })
    }

    @Test
    fun `reset with nothing drawn writes nothing`() = runTest {
        val repo = sixSelected()
        assertEquals(emptyList(), hat(repo).reset(g))
        assertEquals(emptyList(), repo.calls)
    }

    @Test
    fun `undo of a reset restores the drawn set`() = runTest {
        val repo = FakeNameRepository(listOf(name(1, drawn = true), name(2), name(3, selected = false, drawn = true)))
        val hat = hat(repo)
        hat.undoReset(hat.reset(g))
        assertEquals(listOf(true, false, true), repo.all().map { it.drawn })
        assertEquals(listOf(true, true, false), repo.all().map { it.selected })
    }

    @Test
    fun `put back clears drawn and selects only that name`() = runTest {
        val repo = FakeNameRepository(listOf(name(1, selected = false, drawn = true), name(2, selected = false, drawn = true)))
        hat(repo).putBack(1)
        assertEquals(listOf(name(1), name(2, selected = false, drawn = true)), repo.all())
    }

    @Test
    fun `put back selects before clearing drawn so the chip never shows as out`() = runTest {
        val repo = FakeNameRepository(listOf(name(1, selected = false, drawn = true)))
        hat(repo).putBack(1)
        assertEquals(listOf("setSelected(1, true)", "clearDrawn([1])"), repo.calls)
    }

    @Test
    fun `a name added after a draw is in the next pool`() = runTest {
        // AC8.
        val repo = FakeNameRepository(listOf(name(1), name(2)))
        val hat = hat(repo)
        hat.draw(g, 2)
        val added = repo.add(g, "New", selected = true)
        assertEquals(listOf(added), hat.draw(g, 5).map { it.id })
    }

    @Test
    fun `a deleted drawn name takes its drawn state with it`() = runTest {
        // AC8.
        val repo = FakeNameRepository(listOf(name(1), name(2), name(3)))
        val hat = hat(repo)
        val picked = hat.draw(g, 1).single()
        repo.delete(picked.id)
        val state = DrawState.of(repo.all())
        assertEquals(0, state.drawnCount)
        assertEquals(2, state.inHat)
    }
}
