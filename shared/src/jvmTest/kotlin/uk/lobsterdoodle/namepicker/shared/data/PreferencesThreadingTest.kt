package uk.lobsterdoodle.namepicker.shared.data

import java.util.Collections
import java.util.concurrent.Executors
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

/**
 * No preferences file work runs on the caller's thread (on Android, the main thread). Every
 * file-system call DataStore makes is recorded with its thread; with a named store dispatcher,
 * each repository call that touches the file must do so only on that dispatcher.
 *
 * DataStore serves warm reads from memory, so each read is checked cold, on a newly opened
 * store. Every edit reads and writes the file, so writes are checked on a warm store.
 */
class PreferencesThreadingTest {
    @get:Rule
    val tmp = TemporaryFolder()

    // Threads are compared by identity: coroutine debug mode renames them (" @coroutine#1").
    private lateinit var storeThread: Thread
    private val storeDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor { Thread(it, "nam6-settings").also { thread -> storeThread = thread } }
            .asCoroutineDispatcher()
    private val fileSystem = RecordingFileSystem(FileSystem.SYSTEM)

    @After
    fun tearDown() {
        storeDispatcher.close()
    }

    private fun open() = openSettings(tmp.root, CoroutineScope(storeDispatcher + SupervisorJob()), fileSystem)

    private suspend fun assertFileWorkOffCaller(label: String, call: suspend () -> Unit) {
        fileSystem.threads.clear()
        call()
        val threads = fileSystem.threads.toList()
        assertTrue(threads.isNotEmpty(), "$label touched no file: is the recording file system in use?")
        assertTrue(Thread.currentThread() !in threads, "$label did file work on the calling thread: $threads")
        assertTrue(threads.all { it === storeThread }, "$label did file work off the store dispatcher: $threads")
    }

    @Test
    fun `every preferences call does its file work on the store dispatcher and never on the caller`() = runBlocking {
        withTimeout(10_000) {
            // Seed an existing file so cold reads have something to read.
            open().also { it.preferences.setActiveGroup(1) }.closeAndJoin()

            val reads: List<Pair<String, suspend (SettingsStorage) -> Unit>> = listOf(
                "activeGroupId (cold)" to { it.preferences.activeGroupId.first() },
                "sortType (cold)" to { it.preferences.sortType(1).first() },
            )
            for ((label, read) in reads) {
                val settings = open()
                assertFileWorkOffCaller(label) { read(settings) }
                settings.closeAndJoin()
            }

            val settings = open()
            val prefs = settings.preferences
            prefs.activeGroupId.first() // warm the store
            val writes: List<Pair<String, suspend () -> Unit>> = listOf(
                "setActiveGroup" to { prefs.setActiveGroup(2) },
                "clearActiveGroupIf" to { prefs.clearActiveGroupIf(2) },
                "clearActiveGroup" to { prefs.clearActiveGroup() },
                "updateSortType" to { prefs.updateSortType(1) { NameSortType.ASC } },
                "removeSortType" to { prefs.removeSortType(1) },
            )
            for ((label, write) in writes) assertFileWorkOffCaller(label, write)

            // Retired: no file work at all, so certainly none on the caller.
            fileSystem.threads.clear()
            prefs.gridColumns.first()
            prefs.setGridColumns(1)
            assertTrue(Thread.currentThread() !in fileSystem.threads, "gridColumns did file work on the caller")
            settings.closeAndJoin()
        }
    }
}

/** Wraps a file system and records the thread of every call that names a path. */
private class RecordingFileSystem(delegate: FileSystem) : ForwardingFileSystem(delegate) {
    val threads: MutableList<Thread> = Collections.synchronizedList(mutableListOf())

    override fun onPathParameter(path: Path, functionName: String, parameterName: String): Path {
        threads += Thread.currentThread()
        return path
    }
}
