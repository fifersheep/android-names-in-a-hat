package uk.lobsterdoodle.namepicker.shared.data

import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/** Where [openSettings] keeps the settings file inside [dir]. */
internal fun settingsPath(dir: File): Path = File(dir, SETTINGS_FILE_NAME).absolutePath.toPath()

/**
 * Opens the settings file in [dir] through the production [openSettingsStorage], as
 * `SettingsStorage(context)` does on Android but without the legacy migration.
 * Close it with [SettingsStorage.closeAndJoin] before reopening the same file.
 */
internal fun openSettings(
    dir: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    fileSystem: FileSystem = FileSystem.SYSTEM,
): SettingsStorage = openSettingsStorage(path = settingsPath(dir), fileSystem = fileSystem, scope = scope)
