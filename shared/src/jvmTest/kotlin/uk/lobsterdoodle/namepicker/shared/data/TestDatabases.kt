package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.Room
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * Opens [DATABASE_NAME] in [dir] through the production [buildNamesDatabase], the way
 * `NamesStorage(context)` does on Android, so tests exercise the real migrations and settings.
 */
internal fun openStorage(
    dir: File,
    driver: SQLiteDriver = BundledSQLiteDriver(),
    queryContext: CoroutineContext = Dispatchers.IO,
): NamesStorage = NamesStorage(
    Room.databaseBuilder<NamesDatabase>(name = File(dir, DATABASE_NAME).path)
        .buildNamesDatabase(driver, queryContext),
)
