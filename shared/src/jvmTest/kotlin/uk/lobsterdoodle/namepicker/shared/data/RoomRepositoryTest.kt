package uk.lobsterdoodle.namepicker.shared.data

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.model.GroupSummary
import uk.lobsterdoodle.namepicker.shared.model.Name

/** The Room repositories against a real bundled-SQLite file, through the production builder. */
class RoomRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var storage: NamesStorage
    private val groups get() = storage.groups
    private val names get() = storage.names

    @Before
    fun open() {
        storage = openStorage(tmp.root)
    }

    @After
    fun close() {
        storage.close()
    }

    private fun test(block: suspend CoroutineScope.() -> Unit) = runBlocking { withTimeout(10_000) { block() } }

    private suspend fun namesOf(groupId: Long) = names.observeNames(groupId).first()

    @Test
    fun `groups come newest first with their name counts`() = test {
        val a = groups.create("Class A")
        val b = groups.create("Class B")
        names.add(a, "Ann", selected = true)
        names.add(a, "Bob", selected = false)

        assertEquals(
            listOf(GroupSummary(Group(b, "Class B"), 0), GroupSummary(Group(a, "Class A"), 2)),
            groups.observeGroups().first(),
        )
    }

    @Test
    fun `observeGroup follows rename and becomes null after delete`() = test {
        val id = groups.create("Old")
        groups.rename(id, "New")
        assertEquals(Group(id, "New"), groups.observeGroup(id).first())

        groups.delete(id)
        assertNull(groups.observeGroup(id).first())
    }

    @Test
    fun `deleting a group deletes its names and leaves other groups alone`() = test {
        val doomed = groups.create("Doomed")
        val kept = groups.create("Kept")
        names.add(doomed, "Ann", selected = true)
        names.add(doomed, "Bob", selected = true)
        val carl = names.add(kept, "Carl", selected = true)

        assertEquals(Group(doomed, "Doomed"), groups.delete(doomed))

        assertEquals(emptyList(), namesOf(doomed))
        assertEquals(listOf(Name(carl, kept, "Carl", selected = true)), namesOf(kept))
        assertEquals(listOf(GroupSummary(Group(kept, "Kept"), 1)), groups.observeGroups().first())
    }

    @Test
    fun `deleting a missing group returns null`() = test {
        assertNull(groups.delete(99))
    }

    @Test
    fun `ids of deleted groups and names are not reused`() = test {
        val first = groups.create("First")
        val name = names.add(first, "Ann", selected = true)
        groups.delete(first)

        val next = groups.create("Next")
        assertTrue(next > first)
        assertTrue(names.add(next, "Bob", selected = true) > name)
    }

    @Test
    fun `names come in insertion order with the given selected flag and not drawn`() = test {
        val g = groups.create("G")
        val zed = names.add(g, "Zed", selected = true)
        val amy = names.add(g, "Amy", selected = false)

        assertEquals(
            listOf(Name(zed, g, "Zed", selected = true, drawn = false), Name(amy, g, "Amy", selected = false, drawn = false)),
            namesOf(g),
        )
    }

    @Test
    fun `rename changes only the text`() = test {
        val g = groups.create("G")
        val id = names.add(g, "Ann", selected = false)
        names.rename(id, "Anne")
        assertEquals(listOf(Name(id, g, "Anne", selected = false)), namesOf(g))
    }

    @Test
    fun `deleting a name returns it and a missing name returns null`() = test {
        val g = groups.create("G")
        val id = names.add(g, "Ann", selected = true)
        assertEquals(Name(id, g, "Ann", selected = true), names.delete(id))
        assertEquals(emptyList(), namesOf(g))
        assertNull(names.delete(id))
    }

    @Test
    fun `setSelected changes one name and never drawn`() = test {
        val g = groups.create("G")
        val ann = names.add(g, "Ann", selected = true)
        val bob = names.add(g, "Bob", selected = true)
        names.markDrawn(listOf(ann))

        names.setSelected(ann, false)

        assertEquals(
            listOf(Name(ann, g, "Ann", selected = false, drawn = true), Name(bob, g, "Bob", selected = true)),
            namesOf(g),
        )
    }

    @Test
    fun `setAllSelected changes only that group and never drawn`() = test {
        val g = groups.create("G")
        val other = groups.create("Other")
        val ann = names.add(g, "Ann", selected = true)
        val bob = names.add(g, "Bob", selected = false)
        val carl = names.add(other, "Carl", selected = true)
        names.markDrawn(listOf(bob))

        names.setAllSelected(g, false)
        assertEquals(
            listOf(Name(ann, g, "Ann", selected = false), Name(bob, g, "Bob", selected = false, drawn = true)),
            namesOf(g),
        )
        names.setAllSelected(g, true)
        assertEquals(
            listOf(Name(ann, g, "Ann", selected = true), Name(bob, g, "Bob", selected = true, drawn = true)),
            namesOf(g),
        )
        assertEquals(listOf(Name(carl, other, "Carl", selected = true)), namesOf(other))
    }

    @Test
    fun `markDrawn marks only the given names and never changes selected`() = test {
        val g = groups.create("G")
        val ann = names.add(g, "Ann", selected = true)
        val bob = names.add(g, "Bob", selected = false)
        val carl = names.add(g, "Carl", selected = true)

        names.markDrawn(setOf(ann, bob))
        names.markDrawn(emptyList())

        assertEquals(
            listOf(
                Name(ann, g, "Ann", selected = true, drawn = true),
                Name(bob, g, "Bob", selected = false, drawn = true),
                Name(carl, g, "Carl", selected = true, drawn = false),
            ),
            namesOf(g),
        )
    }

    @Test
    fun `clearDrawn clears only that group and never changes selected`() = test {
        val g = groups.create("G")
        val other = groups.create("Other")
        val ann = names.add(g, "Ann", selected = false)
        val carl = names.add(other, "Carl", selected = true)
        names.markDrawn(listOf(ann, carl))

        names.clearDrawn(g)

        assertEquals(listOf(Name(ann, g, "Ann", selected = false, drawn = false)), namesOf(g))
        assertEquals(listOf(Name(carl, other, "Carl", selected = true, drawn = true)), namesOf(other))
    }

    @Test
    fun `the flows emit again after writes`() = test {
        val groupUpdates = Channel<List<GroupSummary>>(Channel.UNLIMITED)
        val g = groups.create("G")
        val nameUpdates = Channel<List<Name>>(Channel.UNLIMITED)
        val collectors = launch(Dispatchers.Default) {
            launch { groups.observeGroups().collect { groupUpdates.send(it) } }
            launch { names.observeNames(g).collect { nameUpdates.send(it) } }
        }
        assertEquals(listOf(GroupSummary(Group(g, "G"), 0)), groupUpdates.receive())
        assertEquals(emptyList(), nameUpdates.receive())

        val ann = names.add(g, "Ann", selected = true)
        assertEquals(listOf(Name(ann, g, "Ann", selected = true)), nameUpdates.receive())
        assertEquals(listOf(GroupSummary(Group(g, "G"), 1)), groupUpdates.receive())

        collectors.cancel()
    }
}
