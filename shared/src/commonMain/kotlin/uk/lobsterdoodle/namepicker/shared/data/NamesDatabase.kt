package uk.lobsterdoodle.namepicker.shared.data

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * The 3.0 database. It opens the `Classrooms.db` file 2.7.0 wrote (legacy schema v2, or v1
 * from before 2017) and migrates it in place; see `Migrations.kt`. Version 3 adds drawn state.
 *
 * The migrations must stay forever: Android backup can restore a 2.7.0 database into any
 * later install.
 */
@Database(entities = [GroupEntity::class, NameEntity::class], version = 3, exportSchema = true)
@ConstructedBy(NamesDatabaseConstructor::class)
internal abstract class NamesDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao

    abstract fun nameDao(): NameDao
}

// Room's KSP processor generates the actual for each target.
@Suppress("KotlinNoActualForExpect")
internal expect object NamesDatabaseConstructor : RoomDatabaseConstructor<NamesDatabase> {
    override fun initialize(): NamesDatabase
}
