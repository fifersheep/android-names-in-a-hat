package uk.lobsterdoodle.namepicker.shared.draw

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import uk.lobsterdoodle.namepicker.shared.model.Name

class NameDrawTest {
    /** Returns a scripted index for each pool size it is asked about, and records the calls. */
    private class ScriptedGenerator(private val indexFor: Map<Int, Int> = emptyMap()) : NumberGenerator {
        val calls = mutableListOf<Int>()
        override fun nextInt(until: Int): Int {
            calls += until
            return indexFor[until] ?: 0
        }
    }

    private val six = listOf("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")

    @Test
    fun `asks the generator once per name drawn`() {
        val generator = ScriptedGenerator()
        NameDraw(generator).draw(listOf("Scott", "Peter", "Rob"), 2)
        assertEquals(listOf(3, 2), generator.calls)
    }

    @Test
    fun `draw one returns the name at the generated index`() {
        val drawn = NameDraw(ScriptedGenerator(mapOf(6 to 2))).draw(six, 1)
        assertEquals(listOf("Rob"), drawn)
    }

    @Test
    fun `each pick is removed before the next`() {
        // Rob is index 2 of six; once removed, Rachel is index 4 of the remaining five.
        val drawn = NameDraw(ScriptedGenerator(mapOf(6 to 2, 5 to 4))).draw(six, 2)
        assertEquals(listOf("Rob", "Rachel"), drawn)
    }

    @Test
    fun `seeded draws are repeatable`() {
        val pool = (1..20).map { "Name $it" }
        val first = NameDraw(RandomNumberGenerator(Random(42))).draw(pool, 5)
        val second = NameDraw(RandomNumberGenerator(Random(42))).draw(pool, 5)
        val third = NameDraw(RandomNumberGenerator(Random(42))).draw(pool, 5)
        assertEquals(first, second)
        assertEquals(first, third)
        assertEquals(5, first.toSet().size)
    }

    @Test
    fun `draw clamps to the pool`() {
        val pool = listOf("Ada", "Grace", "Alan")
        val drawn = NameDraw(RandomNumberGenerator(Random(7))).draw(pool, 5)
        assertEquals(3, drawn.size)
        assertEquals(pool.toSet(), drawn.toSet())
    }

    @Test
    fun `draw of zero is empty`() {
        val generator = ScriptedGenerator()
        assertEquals(emptyList(), NameDraw(generator).draw(six, 0))
        assertTrue(generator.calls.isEmpty())
    }

    @Test
    fun `draw of a negative count is empty`() {
        assertEquals(emptyList(), NameDraw(ScriptedGenerator()).draw(six, -3))
    }

    @Test
    fun `draw from an empty pool is empty`() {
        assertEquals(emptyList(), NameDraw(ScriptedGenerator()).draw(emptyList<String>(), 2))
    }

    @Test
    fun `drawFrom draws only selected names`() {
        val names = listOf(
            name(1, "Ada", selected = false),
            name(2, "Grace", selected = true),
            name(3, "Alan", selected = false),
            name(4, "Edsger", selected = true),
        )
        val drawn = NameDraw(RandomNumberGenerator(Random(3))).drawFrom(names, 4)
        assertEquals(setOf(2L, 4L), drawn.map { it.id }.toSet())
        assertEquals(2, drawn.size)
    }

    private fun name(id: Long, text: String, selected: Boolean) = Name(id, groupId = 1, text = text, selected = selected)
}
