package uk.lobsterdoodle.namepicker.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.lobsterdoodle.namepicker.model.Group;
import uk.lobsterdoodle.namepicker.model.GroupDetails;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.DbHelper;

import static java.util.Arrays.asList;

/** Created by: Scott Laing
 *  Date: 08-May-2014 @ 13:28 */

public class ClassroomDbHelper extends SQLiteOpenHelper implements DbHelper {
    private static ClassroomDbHelper mInstance = null;
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Classrooms.db";

    private static final String TABLE_CLASSROOM = "table_classroom_names";
    private static final String TABLE_PUPIL = "table_pupil_names";
    private static final String COLUMN_CLASSROOM_ID = "column_classroom_ID";
    private static final String COLUMN_CLASSROOM_NAME = "column_classroom_name";
    private static final String COLUMN_PUPIL_ID = "column_pupil_ID";
    private static final String COLUMN_PUPIL_NAME = "column_pupil_name";

    private static final String TABLE_GROUPS = "table_groups";
    private static final String COLUMN_GROUP_ID = "column_group_id";
    private static final String COLUMN_GROUP_NAME = "column_group_name";
    private static final String TABLE_NAMES = "table_names";
    private static final String COLUMN_NAME_ID = "column_name_ID";
    private static final String COLUMN_NAME_NAME = "column_name_name";
    private static final String COLUMN_NAME_TOGGLED = "column_name_toggled";

    public static ClassroomDbHelper getInstance(Context context) {
        if (mInstance == null) mInstance = new ClassroomDbHelper(context.getApplicationContext());
        return mInstance;
    }

    private ClassroomDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GROUPS_TABLE);
        db.execSQL(CREATE_NAMES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);

        db.execSQL(
                "INSERT INTO " + TABLE_GROUPS + "(" + COLUMN_GROUP_ID + ", " + COLUMN_GROUP_NAME + ") " +
                        "SELECT " + COLUMN_CLASSROOM_ID + ", " + COLUMN_CLASSROOM_NAME + " FROM " + TABLE_CLASSROOM);

        db.execSQL("INSERT INTO " + TABLE_NAMES + "(" + COLUMN_NAME_ID + ", " + COLUMN_NAME_NAME + ", " + COLUMN_GROUP_ID + ") " +
                "SELECT " + COLUMN_PUPIL_ID + ", " + COLUMN_PUPIL_NAME + ", " + COLUMN_CLASSROOM_ID + " FROM " + TABLE_PUPIL);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSROOM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUPIL);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /** Database creation/deletion SQL statements **/
    private static final String CREATE_GROUPS_TABLE = "create table " + TABLE_GROUPS + "("
            + COLUMN_GROUP_ID + " integer primary key autoincrement, "
            + COLUMN_GROUP_NAME + " text not null);";

    private static final String CREATE_NAMES_TABLE = "create table " + TABLE_NAMES + "("
            + COLUMN_NAME_ID + " integer primary key autoincrement, "
            + COLUMN_NAME_NAME + " text not null, "
            + COLUMN_NAME_TOGGLED + " boolean default false, "
            + COLUMN_GROUP_ID + " references "
            + TABLE_GROUPS + "(" + COLUMN_GROUP_ID + "));";

    @Override
    public long createGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        long groupId = -1;

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);

        if (db != null) {
            groupId = db.insert(TABLE_GROUPS, null, values);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > createGroup()");
        }
        return groupId;
    }

    @Override
    public GroupDetails retrieveGroupDetails(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        GroupDetails group = null;

        String[] projection = { COLUMN_GROUP_ID, COLUMN_GROUP_NAME };
        String selection = COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_GROUPS, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();
            group = new GroupDetails(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
            cursor.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupil()");
        }
        return group;
    }

    @Override
    public void addClassroom (String classroomName, List<String> pupils) {
//        createGroup(classroomName);
//        for (String pupil : pupils) {
//            addPupil(classroomName, pupil);
//        }
    }

    @Override
    public void editGroupNames(long groupId, List<String> names) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        if (db != null) {
//            String[] selectionArgs = { COLUMN_CLASSROOM_ID, String.valueOf(groupId) };
//            db.delete(TABLE_PUPIL, "? LIKE ?", selectionArgs);
//
//            for (String name : names) {
//                ContentValues values = new ContentValues();
//                values.put(COLUMN_CLASSROOM_ID, groupId);
//                values.put(COLUMN_PUPIL_NAME, name);
//                db.insert(TABLE_PUPIL, null, values);
//            }
//
//            db.close();
//        } else {
//            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > emptyClassroom()");
//        }
    }

    @Override
    public void editGroupName(long groupId, String newName){
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, newName);

        if (db != null) {
            db.update(TABLE_GROUPS, values, selection, selectionArgs);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > editGroupName()");
        }
    }

    @Override
    public GroupDetails removeGroup(long groupId) {
        removeNamesForGroup(groupId);
        SQLiteDatabase db = this.getReadableDatabase();
        GroupDetails deletedGroup = null;

        String[] projection = { COLUMN_GROUP_ID, COLUMN_GROUP_NAME };
        String selection = COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_GROUPS, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();
            deletedGroup = new GroupDetails(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
            cursor.close();


            db.delete(TABLE_GROUPS, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removeGroup()");
        }
        return deletedGroup;

        // Reset classroom id counter table is empty
        //TODO: Reset counter for classroom id in SQLite db table when empty
    }

    private void removeNamesForGroup(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };

        // Issue SQL statement.
        if (db != null) {
            db.delete(TABLE_NAMES, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupilsForGroup()");
        }
    }

    @Override
    public void addNameToGroup(long groupId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_ID, groupId);
        values.put(COLUMN_NAME_NAME, name);

        if (db != null) {
            db.insert(TABLE_NAMES, null, values);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > addNameToGroup()");
        }
    }

    @Override
    public Name removeName(long nameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Name deletedName = null;

        String[] projection = { COLUMN_NAME_ID, COLUMN_NAME_NAME, COLUMN_NAME_TOGGLED };
        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(nameId) };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_NAMES, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();
            deletedName = new Name(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_TOGGLED)) > 0);
            cursor.close();

            db.delete(TABLE_NAMES, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupil()");
        }
        return deletedName;

        // Reset pupil id counter table is empty
        //TODO: Reset counter for pupil id in SQLite db table when empty
    }

    @Override
    public void updateName(Name name) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, name.name);
        values.put(COLUMN_NAME_TOGGLED, name.toggledOn ? 1 : 0);

        String selection = COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(name.id)};

        if (db != null) {
            db.update(TABLE_NAMES, values, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > updatePupilName()");
        }
    }

    @Override
    public long getClassroomId(String classroomName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long classroomId = -1;

        String[] projection = { COLUMN_GROUP_ID };
        String selection = COLUMN_GROUP_NAME + " LIKE ?";
        String[] selectionArgs = { classroomName };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_GROUPS, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            try {
                classroomId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID));
            } catch (CursorIndexOutOfBoundsException e) {
                Log.e("Names in a Hat",  "CursorIndexOutOfBounds: " + getClass().getName() + " > getClassroomId()");
            } finally {
                cursor.close();
            }
        }
        return classroomId;
    }

    @Override
    public List<Group> getAllGroups() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Group> groups = new ArrayList<>();

        if (db != null) {
            LongSparseArray<String> groupNamesById = new LongSparseArray<>();
            String sortOrder = COLUMN_GROUP_ID + " DESC";
            Cursor cursor = db.query(TABLE_GROUPS, arr(COLUMN_GROUP_ID, COLUMN_GROUP_NAME), null, null, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                groupNamesById.put(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
                cursor.moveToNext();
            }
            cursor.close();

            for (int i = 0; i < groupNamesById.size(); i++) {
                final long groupId = groupNamesById.keyAt(i);
                groups.add(new Group(new GroupDetails(groupId, groupNamesById.valueAt(i)), retrieveGroupNames(groupId)));
            }
        }
        return groups;
    }

    public List<Name> retrieveGroupNames(long classroomId) {
        List<Name> pupilNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_NAME_ID, COLUMN_NAME_NAME, COLUMN_NAME_TOGGLED };
        String selection = COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(classroomId) };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_NAMES, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                pupilNames.add(new Name(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_TOGGLED)) > 0));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return pupilNames;
    }

    public boolean sortPupils(String groupId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_NAME_ID, COLUMN_NAME_NAME };
        String[] selectionArgs = { COLUMN_GROUP_ID, groupId };
        String sortOrder = COLUMN_GROUP_ID + " DESC";

        if (db != null) {
            List<String> originalNames = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            Cursor cursor = db.query(TABLE_NAMES, projection, "?=?", selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                originalNames.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME)));
                ids.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
                cursor.moveToNext();
            }
            cursor.close();

            List<String> sortedNames = new ArrayList<>(originalNames);
            Collections.sort(sortedNames);

            for (int i = 0; i < originalNames.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_NAME, sortedNames.get(i));

                String sel = COLUMN_NAME_ID + " LIKE ?";
                String[] selArgs = { ids.get(i) };

                db.update(TABLE_NAMES, values, sel, selArgs);
            }
            return true;
        } else {
            return false;
        }
    }

    private String[] arr(String... items) {
        return asList(items).toArray(new String[items.length]);
    }
}