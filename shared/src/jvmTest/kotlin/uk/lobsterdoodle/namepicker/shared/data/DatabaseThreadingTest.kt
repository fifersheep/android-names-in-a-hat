package uk.lobsterdoodle.namepicker.shared.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.test.assertTrue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * No database work runs on the caller's thread (on Android, the main thread). Every statement
 * Room prepares is recorded with its thread; with a named query dispatcher, each repository
 * call must prepare at least one statement, and all of them on that dispatcher.
 */
class DatabaseThreadingTest {
    @get:Rule
    val tmp = TemporaryFolder()

    // Threads are compared by identity: coroutine debug mode renames them (" @coroutine#1").
    private lateinit var dbThread: Thread
    private val dbDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor { Thread(it, "nam5-db").also { thread -> dbThread = thread } }
            .asCoroutineDispatcher()
    private val driver = RecordingDriver(BundledSQLiteDriver())

    @After
    fun tearDown() {
        dbDispatcher.close()
    }

    @Test
    fun `every repository call runs its SQL on the query dispatcher and never on the caller`() = runBlocking {
        withTimeout(10_000) {
            openStorage(tmp.root, driver, dbDispatcher).use { storage ->
                val groups = storage.groups
                val names = storage.names
                var groupId = 0L
                var nameId = 0L

                val calls: List<Pair<String, suspend () -> Unit>> = listOf(
                    "GroupRepository.create" to { groupId = groups.create("G") },
                    "GroupRepository.observeGroups" to { groups.observeGroups().first() },
                    "GroupRepository.observeGroup" to { groups.observeGroup(groupId).first() },
                    "GroupRepository.rename" to { groups.rename(groupId, "H") },
                    "NameRepository.add" to { nameId = names.add(groupId, "Ann", selected = true) },
                    "NameRepository.observeNames" to { names.observeNames(groupId).first() },
                    "NameRepository.rename" to { names.rename(nameId, "Anne") },
                    "NameRepository.setSelected" to { names.setSelected(nameId, false) },
                    "NameRepository.setAllSelected" to { names.setAllSelected(groupId, true) },
                    "NameRepository.markDrawn" to { names.markDrawn(listOf(nameId)) },
                    "NameRepository.clearDrawn" to { names.clearDrawn(groupId) },
                    "NameRepository.delete" to { names.delete(nameId) },
                    "GroupRepository.delete" to { groups.delete(groupId) },
                )

                val caller = Thread.currentThread()
                for ((label, call) in calls) {
                    driver.threads.clear()
                    call()
                    val threads = driver.threads.toList()
                    assertTrue(threads.isNotEmpty(), "$label prepared no statements: is the recording driver in use?")
                    assertTrue(caller !in threads, "$label ran SQL on the calling thread $caller: $threads")
                    assertTrue(threads.all { it === dbThread }, "$label ran SQL off the query dispatcher: $threads")
                }
            }
        }
    }
}

/** Wraps a driver and records the thread on which every statement is prepared. */
private class RecordingDriver(private val delegate: SQLiteDriver) : SQLiteDriver by delegate {
    val threads: MutableList<Thread> = Collections.synchronizedList(mutableListOf())

    override fun open(fileName: String): SQLiteConnection = RecordingConnection(delegate.open(fileName), threads)
}

private class RecordingConnection(
    private val delegate: SQLiteConnection,
    private val threads: MutableList<Thread>,
) : SQLiteConnection by delegate {
    override fun prepare(sql: String): SQLiteStatement {
        threads += Thread.currentThread()
        return delegate.prepare(sql)
    }
}
