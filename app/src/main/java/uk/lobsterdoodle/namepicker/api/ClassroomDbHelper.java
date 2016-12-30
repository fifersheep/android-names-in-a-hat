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
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.DbHelper;

import static java.util.Arrays.asList;

/** Created by: Scott Laing
 *  Date: 08-May-2014 @ 13:28 */

public class ClassroomDbHelper extends SQLiteOpenHelper implements DbHelper {
    private static ClassroomDbHelper mInstance = null;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Classrooms.db";

    private static final String TABLE_CLASSROOM = "table_classroom_names";
    private static final String TABLE_PUPIL = "table_pupil_names";
    private static final String COLUMN_CLASSROOM_ID = "column_classroom_ID";
    private static final String COLUMN_CLASSROOM_NAME = "column_classroom_name";
    private static final String COLUMN_PUPIL_ID = "column_pupil_ID";
    private static final String COLUMN_PUPIL_NAME = "column_pupil_name";

    public static ClassroomDbHelper getInstance(Context context) {
        if (mInstance == null) mInstance = new ClassroomDbHelper(context.getApplicationContext());
        return mInstance;
    }

    private ClassroomDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CLASSROOM_TABLE);
        db.execSQL(CREATE_PUPIL_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DELETE_CLASSROOM_TABLE);
        db.execSQL(DELETE_PUPIL_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /** Database creation/deletion SQL statements **/
    private static final String CREATE_CLASSROOM_TABLE = "create table " + TABLE_CLASSROOM + "("
            + COLUMN_CLASSROOM_ID + " integer primary key autoincrement, "
            + COLUMN_CLASSROOM_NAME + " text not null);";

    private static final String CREATE_PUPIL_TABLE = "create table " + TABLE_PUPIL + "("
            + COLUMN_PUPIL_ID + " integer primary key autoincrement, "
            + COLUMN_PUPIL_NAME + " text not null, "
            + COLUMN_CLASSROOM_ID + " references "
            + TABLE_CLASSROOM + "(" + COLUMN_CLASSROOM_ID + "));";

    private static final String DELETE_CLASSROOM_TABLE = "DROP TABLE IF EXISTS " + TABLE_CLASSROOM;
    private static final String DELETE_PUPIL_TABLE = "DROP TABLE IF EXISTS " + TABLE_PUPIL;

    @Override
    public long createGroup(String classroomName) {
        SQLiteDatabase db = this.getWritableDatabase();
        long classroomId = -1;

        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASSROOM_NAME, classroomName);

        if (db != null) {
            classroomId = db.insert(TABLE_CLASSROOM, null, values);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > createGroup()");
        }
        return classroomId;
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
    public void updateClassroomName(String originalName, String newName){
//        SQLiteDatabase db = this.getReadableDatabase();
//        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";
//        String[] selectionArgs = { String.valueOf(getClassroomId(originalName)) };
//
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_CLASSROOM_NAME, newName);
//
//        if (db != null) {
//            db.update(TABLE_CLASSROOM, values, selection, selectionArgs);
//        } else {
//            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > updateClassroomName()");
//        }
    }

    @Override
    public Group removeGroup(long groupId) {
        removePupilsForGroup(groupId);
        SQLiteDatabase db = this.getReadableDatabase();
        Group deletedGroup = null;

        String[] projection = { COLUMN_CLASSROOM_ID, COLUMN_CLASSROOM_NAME };
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_CLASSROOM, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();
            deletedGroup = new Group(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_NAME)),
                    Collections.emptyList());
            cursor.close();


            db.delete(TABLE_CLASSROOM, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removeGroup()");
        }
        return deletedGroup;

        // Reset classroom id counter table is empty
        //TODO: Reset counter for classroom id in SQLite db table when empty
    }

    private void removePupilsForGroup(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(groupId) };

        // Issue SQL statement.
        if (db != null) {
            db.delete(TABLE_PUPIL, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupilsForGroup()");
        }
    }

    @Override
    public void addNameToGroup(long groupId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASSROOM_ID, groupId);
        values.put(COLUMN_PUPIL_NAME, name);

        if (db != null) {
            db.insert(TABLE_PUPIL, null, values);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > addNameToGroup()");
        }
    }

    @Override
    public Name removeName(long nameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Name deletedName = null;

        String[] projection = { COLUMN_PUPIL_ID, COLUMN_PUPIL_NAME };
        String selection = COLUMN_PUPIL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(nameId) };
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_PUPIL, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();
            deletedName = new Name(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_NAME)));
            cursor.close();

            db.delete(TABLE_PUPIL, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupil()");
        }
        return deletedName;

        // Reset pupil id counter table is empty
        //TODO: Reset counter for pupil id in SQLite db table when empty
    }

    @Override
    public void updatePupilName(String currentName, String classroomName, String newName) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PUPIL_NAME, newName);

        String selection = COLUMN_PUPIL_NAME + " LIKE ? AND " + COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { currentName, String.valueOf(getClassroomId(classroomName))};

        if (db != null) {
            db.update(TABLE_PUPIL, values, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > updatePupilName()");
        }
    }

    @Override
    public long getClassroomId(String classroomName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long classroomId = -1;

        String[] projection = { COLUMN_CLASSROOM_ID };
        String selection = COLUMN_CLASSROOM_NAME + " LIKE ?";
        String[] selectionArgs = { classroomName };
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_CLASSROOM, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            try {
                classroomId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_ID));
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
            String sortOrder = COLUMN_CLASSROOM_ID + " DESC";
            Cursor cursor = db.query(TABLE_CLASSROOM, arr(COLUMN_CLASSROOM_ID, COLUMN_CLASSROOM_NAME), null, null, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                groupNamesById.put(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_NAME)));
                cursor.moveToNext();
            }
            cursor.close();

            for (int i = 0; i < groupNamesById.size(); i++) {
                final long groupId = groupNamesById.keyAt(i);
                groups.add(new Group(groupId, groupNamesById.valueAt(i), retrieveGroupNames(groupId)));
            }
        }
        return groups;
    }

    public List<Name> retrieveGroupNames(long classroomId) {
        List<Name> pupilNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_PUPIL_ID, COLUMN_PUPIL_NAME };
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(classroomId) };
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.query(TABLE_PUPIL, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                pupilNames.add(new Name(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_NAME))));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return pupilNames;
    }

    public boolean sortPupils(String groupId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_PUPIL_ID, COLUMN_PUPIL_NAME };
        String[] selectionArgs = { COLUMN_CLASSROOM_ID, groupId };
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        if (db != null) {
            List<String> originalNames = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            Cursor cursor = db.query(TABLE_PUPIL, projection, "?=?", selectionArgs, null, null, sortOrder);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                originalNames.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_NAME)));
                ids.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUPIL_ID)));
                cursor.moveToNext();
            }
            cursor.close();

            List<String> sortedNames = new ArrayList<>(originalNames);
            Collections.sort(sortedNames);

            for (int i = 0; i < originalNames.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_PUPIL_NAME, sortedNames.get(i));

                String sel = COLUMN_PUPIL_ID + " LIKE ?";
                String[] selArgs = { ids.get(i) };

                db.update(TABLE_PUPIL, values, sel, selArgs);
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