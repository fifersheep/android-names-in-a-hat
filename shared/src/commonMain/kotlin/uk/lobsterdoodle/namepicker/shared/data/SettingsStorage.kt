package uk.lobsterdoodle.namepicker.shared.data

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import okio.FileSystem
import okio.Path
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** The settings file. DataStore requires the `.preferences_pb` extension. */
internal const val SETTINGS_FILE_NAME = "settings.preferences_pb"

/**
 * The app's small per-install settings. DataStore stays an implementation detail of `:shared`:
 * callers see only [PreferencesRepository]. On Android, create one per process with
 * `SettingsStorage(context)` and keep it for the app's lifetime (a singleton in the DI graph):
 * two instances on one file throw on first use.
 */
class SettingsStorage internal constructor(
    internal val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : AutoCloseable {
    val preferences: PreferencesRepository = DataStorePreferencesRepository(dataStore)

    /** Stops the store for good. Every later read or write fails. */
    override fun close() = scope.cancel()

    /** Closes and waits until the file is released, so the same file can be opened again. */
    internal suspend fun closeAndJoin() = scope.coroutineContext.job.cancelAndJoin()
}

/**
 * Builds the settings store the same way on every platform. All file work runs in [scope], which
 * must live as long as the store (cancelling it stops the store). [migrations] run once, on first
 * use. A corrupt file is replaced with empty settings rather than failing every read.
 */
internal fun openSettingsStorage(
    path: Path,
    fileSystem: FileSystem,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): SettingsStorage = SettingsStorage(
    PreferenceDataStoreFactory.create(
        storage = OkioStorage(fileSystem, PreferencesSerializer) { path },
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        migrations = migrations,
        scope = scope,
    ),
    scope,
)
