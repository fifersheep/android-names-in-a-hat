package uk.lobsterdoodle.namepicker.shared.data

import android.content.Context
import androidx.datastore.dataStoreFile
import okio.FileSystem
import okio.Path.Companion.toOkioPath

/**
 * Opens the app's settings (`files/datastore/settings.preferences_pb`). On first use it moves
 * 2.7.0's `name_list` SharedPreferences settings across and deletes that file.
 * Call once per process and keep the result for the app's lifetime; see [SettingsStorage].
 */
fun SettingsStorage(context: Context): SettingsStorage {
    val app = context.applicationContext
    return openSettingsStorage(
        fileSystem = FileSystem.SYSTEM,
        migrations = listOf(legacyNameListMigration(app)),
        // Lazy: getFilesDir() stats (and may create) a directory, so it runs on the store's scope.
        producePath = { app.dataStoreFile(SETTINGS_FILE_NAME).toOkioPath() },
    )
}
