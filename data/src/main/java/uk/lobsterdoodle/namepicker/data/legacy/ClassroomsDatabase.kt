package uk.lobsterdoodle.namepicker.data.legacy

import android.content.Context
import androidx.room.*
import androidx.room.Database
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = 2,
    entities = [GroupEntity::class, NameEntity::class],
    autoMigrations = [
            AutoMigration (
                from = 1,
                to = 2,
                spec = ClassroomsDatabase.ClassroomsAutoMigration::class
            )
    ]
)
abstract class ClassroomsDatabase : RoomDatabase() {

    abstract fun groupsDao(): GroupsDao

    abstract fun namesDao(): NamesDao

    companion object {
        fun init(applicationContext: Context) {
            Room.databaseBuilder(applicationContext, ClassroomsDatabase::class.java, "Classrooms").build()
        }
    }

    @RenameTable(fromTableName = Table.CLASSROOM, toTableName = Table.GROUPS)
    @RenameTable(fromTableName = Table.PUPIL, toTableName = Table.NAMES)
    @RenameColumn(tableName = Table.GROUPS, fromColumnName = Column.CLASSROOM_ID, toColumnName = Column.GROUP_ID)
    @RenameColumn(tableName = Table.GROUPS, fromColumnName = Column.CLASSROOM_NAME, toColumnName = Column.GROUP_NAME)
    @RenameColumn(tableName = Table.NAMES, fromColumnName = Column.PUPIL_ID, toColumnName = Column.NAME_ID)
    @RenameColumn(tableName = Table.NAMES, fromColumnName = Column.PUPIL_NAME, toColumnName = Column.NAME_NAME)
    @RenameColumn(tableName = Table.NAMES, fromColumnName = Column.GROUP_ID, toColumnName = Column.CLASSROOM_ID)
    class ClassroomsAutoMigration : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                ALTER TABLE ${Table.NAMES}
                ADD COLUMN ${Column.NAME_TOGGLED} BOOLEAN NOT NULL
                DEFAULT 0
            """.trimIndent())
        }
    }
}

class Table {
    companion object {
        const val CLASSROOM = "table_classroom_names"
        const val PUPIL = "table_pupil_names"

        const val GROUPS = "table_groups"
        const val NAMES = "table_names"
    }
}

class Column {
    companion object {
        const val CLASSROOM_ID = "column_classroom_ID"
        const val CLASSROOM_NAME = "column_classroom_name"
        const val PUPIL_ID = "column_pupil_ID"
        const val PUPIL_NAME = "column_pupil_name"

        const val GROUP_ID = "column_group_id"
        const val GROUP_NAME = "column_group_name"
        const val NAME_ID = "column_name_ID"
        const val NAME_NAME = "column_name_name"
        const val NAME_TOGGLED = "column_name_toggled"
    }
}
