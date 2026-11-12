package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * The file 2.7.0 and every earlier release wrote. Changing it would make Room create a new,
 * empty database and every existing user would lose their groups.
 */
internal const val DATABASE_NAME = "Classrooms.db"

/**
 * Finishes a platform builder the same way everywhere: bundled SQLite, every migration, and
 * all queries on [queryContext]. There is deliberately no destructive fallback: a missing
 * migration must crash, not wipe user data.
 */
internal fun RoomDatabase.Builder<NamesDatabase>.buildNamesDatabase(
    driver: SQLiteDriver = BundledSQLiteDriver(),
    queryContext: CoroutineContext = Dispatchers.IO,
): NamesDatabase = addMigrations(*ALL_MIGRATIONS)
    .setDriver(driver)
    .setQueryCoroutineContext(queryContext)
    .build()
