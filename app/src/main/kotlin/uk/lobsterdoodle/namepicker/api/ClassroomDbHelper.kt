package uk.lobsterdoodle.namepicker.api

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import uk.lobsterdoodle.namepicker.model.Group
import uk.lobsterdoodle.namepicker.model.GroupDetails
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.storage.DbHelper
import java.util.*

class ClassroomDbHelper constructor(context: Context) :
        SQLiteOpenHelper(context, Database.NAME, null, Database.VERSION), DbHelper {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_GROUPS_TABLE)
        db.execSQL(CREATE_NAMES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)

        db.execSQL(
            "INSERT INTO ${Table.GROUPS}(${Column.GROUP_ID}, ${Column.GROUP_NAME}) " +
                "SELECT ${Column.CLASSROOM_ID}, ${Column.CLASSROOM_NAME} FROM ${Table.CLASSROOM}")

        db.execSQL(
            "INSERT INTO ${Table.NAMES}(${Column.NAME_ID}, ${Column.NAME_NAME}, ${Column.GROUP_ID}) " +
                "SELECT ${Column.PUPIL_ID}, ${Column.PUPIL_NAME}, ${Column.CLASSROOM_ID} FROM ${Table.PUPIL}")

        db.execSQL("DROP TABLE IF EXISTS ${Table.CLASSROOM}")
        db.execSQL("DROP TABLE IF EXISTS ${Table.PUPIL}")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun createGroup(groupName: String): Long {
        val db = writableDatabase
        return db?.let {
            val values = ContentValues()
            values.put(Column.GROUP_NAME, groupName)
            val groupId = db.insert(Table.GROUPS, null, values)
            db.close()
            groupId
        } ?: -1L
    }

    override fun retrieveGroupDetails(groupId: Long): GroupDetails? {
        val db = this.readableDatabase
        return db?.let {
            val projection = arrayOf(Column.GROUP_ID, Column.GROUP_NAME)
            val selection = " ${Column.GROUP_ID} = $groupId "
            val sortOrder = "${Column.GROUP_ID} DESC"
            val cursor = db.query(Table.GROUPS, projection, selection, emptyArray(), null, null, sortOrder)
            cursor.moveToFirst()
            val details = GroupDetails(
                    cursor.getLong(cursor.getColumnIndexOrThrow(Column.GROUP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(Column.GROUP_NAME)))
            cursor.close()
            details
        }
    }

    override fun editGroupName(groupId: Long, newName: String) {
        val db = this.readableDatabase
        val selection = " ${Column.GROUP_ID} = $groupId "
        val values = ContentValues()
        values.put(Column.GROUP_NAME, newName)
        db?.update(Table.GROUPS, values, selection, emptyArray())
    }

    override fun removeGroup(groupId: Long): GroupDetails? {
        val db = this.readableDatabase
        return db?.let {
            val projection = arrayOf(Column.GROUP_ID, Column.GROUP_NAME)
            val selection = " ${Column.GROUP_ID} = $groupId "
            val sortOrder = "${Column.GROUP_ID} DESC"
            db.delete(Table.NAMES, selection, emptyArray())

            val cursor = db.query(Table.GROUPS, projection, selection, emptyArray(), null, null, sortOrder)
            cursor.moveToFirst()
            val deletedGroup = GroupDetails(
                    cursor.getLong(cursor.getColumnIndexOrThrow(Column.GROUP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(Column.GROUP_NAME)))
            cursor.close()

            db.delete(Table.GROUPS, selection, emptyArray())
            db.close()
            deletedGroup
        }

        // Reset classroom id counter table is empty
        //TODO: Reset counter for classroom id in SQLite db table when empty
    }

    override fun addNameToGroup(groupId: Long, name: String): Long {
        val db = this.writableDatabase
        return db?.let {
            val values = ContentValues()
            values.put(Column.GROUP_ID, groupId)
            values.put(Column.NAME_NAME, name)
            values.put(Column.NAME_TOGGLED, 1)
            val nameId = db.insert(Table.NAMES, null, values)
            db.close()
            nameId
        } ?: -1L
    }

    override fun removeName(nameId: Long): Name? {
        val db = this.readableDatabase
        return db?.let {
            val projection = arrayOf(Column.NAME_ID, Column.NAME_NAME, Column.NAME_TOGGLED)
            val selection = " ${Column.NAME_ID} = $nameId "
            val sortOrder = "${Column.GROUP_ID} DESC"
            val cursor = db.query(Table.NAMES, projection, selection, emptyArray(), null, null, sortOrder)
            cursor.moveToFirst()
            val deletedName = Name(
                    cursor.getLong(cursor.getColumnIndexOrThrow(Column.NAME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(Column.NAME_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(Column.NAME_TOGGLED)) > 0)
            cursor.close()

            db.delete(Table.NAMES, selection, emptyArray())
            db.close()
            deletedName
        }

        // Reset pupil id counter table is empty
        //TODO: Reset counter for pupil id in SQLite db table when empty
    }

    override fun updateName(name: Name) {
        val db = this.readableDatabase
        db?.let {
            val selection = " ${Column.NAME_ID} = ${name.id} "
            val values = ContentValues()
            values.put(Column.NAME_NAME, name.name)
            values.put(Column.NAME_TOGGLED, if (name.toggledOn) 1 else 0)
            db.update(Table.NAMES, values, selection, emptyArray())
            db.close()
        }
    }

    override val allGroups: List<Group>
        get() {
            val db = this.readableDatabase
            return db?.let {
                val sortOrder = "${Column.GROUP_ID} DESC"
                val cursor = db.query(Table.GROUPS, arrayOf(Column.GROUP_ID, Column.GROUP_NAME), null, null, null, null, sortOrder)
                cursor.moveToFirst()

                val groupDetails = ArrayList<GroupDetails>()
                for (i in 0..cursor.count - 1) {
                    groupDetails.add(GroupDetails(
                            cursor.getLong(cursor.getColumnIndexOrThrow(Column.GROUP_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Column.GROUP_NAME))))
                    cursor.moveToNext()
                }
                cursor.close()
                groupDetails.map { Group(it, retrieveGroupNames(it.id)) }
            } ?: emptyList()
        }

    override fun retrieveGroupNames(groupId: Long): List<Name> {
        val db = this.readableDatabase
        return db?.let {
            val projection = arrayOf(Column.NAME_ID, Column.NAME_NAME, Column.NAME_TOGGLED)
            val selection = " ${Column.GROUP_ID} = $groupId "
            val sortOrder = "${Column.GROUP_ID} DESC"
            val cursor = db.query(Table.NAMES, projection, selection, emptyArray(), null, null, sortOrder)
            cursor.moveToFirst()

            val names = ArrayList<Name>()
            for (i in 0..cursor.count - 1) {
                names.add(Name(
                        cursor.getLong(cursor.getColumnIndexOrThrow(Column.NAME_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Column.NAME_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Column.NAME_TOGGLED)) > 0))
                cursor.moveToNext()
            }
            cursor.close()
            names
        } ?: emptyList()
    }

    override fun toggleAllNamesInGroup(groupId: Long, toggleOn: Boolean) {
        val db = this.writableDatabase
        db?.let {
            val selection = " ${Column.GROUP_ID} = $groupId "
            val values = ContentValues()
            values.put(Column.NAME_TOGGLED, if (toggleOn) 1 else 0)
            db.update(Table.NAMES, values, selection, emptyArray())
            db.close()
        }
    }

    companion object {
        private val CREATE_GROUPS_TABLE = "create table ${Table.GROUPS}(" +
            "${Column.GROUP_ID} integer primary key autoincrement, " +
            "${Column.GROUP_NAME} text not null);"

        private val CREATE_NAMES_TABLE = "create table ${Table.NAMES}(" +
            "${Column.NAME_ID} integer primary key autoincrement, " +
            "${Column.NAME_NAME} text not null, " +
            "${Column.NAME_TOGGLED} boolean default false, " +
            "${Column.GROUP_ID} references " +
            "${Table.GROUPS}(${Column.GROUP_ID}));"
    }

    class Database {
        companion object {
            const val VERSION = 2
            const val NAME = "Classrooms.db"
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
}