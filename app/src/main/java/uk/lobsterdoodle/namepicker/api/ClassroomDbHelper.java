package uk.lobsterdoodle.namepicker.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/** Created by: Scott Laing
 *  Date: 08-May-2014 @ 13:28 */

class ClassroomDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
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
        if (mInstance == null)
            mInstance = new ClassroomDbHelper(context.getApplicationContext());
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

    private static final String DELETE_CLASSROOM_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_CLASSROOM;

    private static final String DELETE_PUPIL_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_PUPIL;

    public void addClassroom(String classroomName) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASSROOM_NAME, classroomName);

        // Insert the new row
        if (db != null) {
            db.insert(TABLE_CLASSROOM, null, values);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > addClassroom()");
        }
    }

    public void addClassroom (String classroomName, ArrayList<Pupil> pupils) {
        // Add classroom to the classrooms table
        addClassroom(classroomName);

        // Add each pupil to the database
        for (Pupil pupil : pupils) {
            addPupil(classroomName, pupil.getName());
        }
    }

    public void updateClassroomName(String originalName, String newName){
        // Put the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Get the classroom id using the classroom name
        long classroomId = getClassroomId(originalName);

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASSROOM_NAME, newName);

        // Which row to update, based on the ID
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(classroomId) };

        if (db != null) {
            db.update(
                TABLE_CLASSROOM,
                values,
                selection,
                selectionArgs);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > updateClassroomName()");
        }
    }

    public void removeClassroom(String classroomName) {
        emptyClassroom(classroomName);

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_CLASSROOM_NAME + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { classroomName };

        // Issue SQL statement.
        if (db != null) {
            db.delete(TABLE_CLASSROOM, selection, selectionArgs);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removeClassroom()");
        }

        // Reset classroom id counter table is empty
        //TODO: Reset counter for classroom id in SQLite db table when empty
    }

    private void emptyClassroom(String classroomName) {
        // Retrieve the classroom's id
        long classroomId = getClassroomId(classroomName);

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(classroomId) };

        // Issue SQL statement.
        if (db != null) {
            db.delete(TABLE_PUPIL, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > emptyClassroom()");
        }
    }

    public void addPupil(String classroomName, String pupilName) {

        // Retrieve the classroom's id
        long classroomId = getClassroomId(classroomName);

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASSROOM_ID, classroomId);
        values.put(COLUMN_PUPIL_NAME, pupilName);

        // Insert the new row, returning the primary key value of the new row
        if (db != null) {
            db.insert(TABLE_PUPIL, null, values);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > addPupil()");
        }
    }

    public void removePupil(String pupilName, String classroomName) {

        // Retrieve the classroom's id
        long classroomId = getClassroomId(classroomName);

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_PUPIL_NAME + " LIKE ? AND " + COLUMN_CLASSROOM_ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { pupilName, String.valueOf(classroomId) };

        // Issue SQL statement.
        if (db != null) {
            db.delete(TABLE_PUPIL, selection, selectionArgs);
            db.close();
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > removePupil()");
        }

        // Reset pupil id counter table is empty
        //TODO: Reset counter for pupil id in SQLite db table when empty
    }

    public void updatePupilName(String currentName, String classroomName, String newName) {
        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Get the classroom id using the classroom name
        long classroomId = getClassroomId(classroomName);

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(COLUMN_PUPIL_NAME, newName);

        // Which row to update, based on the ID
        String selection = COLUMN_PUPIL_NAME + " LIKE ? AND " + COLUMN_CLASSROOM_ID + " LIKE ?";
        String[] selectionArgs = { currentName, String.valueOf(classroomId)};

        // Run the database update query
        if (db != null) {
            db.update(TABLE_PUPIL,
                    values,
                    selection,
                    selectionArgs);
        } else {
            Log.e("Names in a Hat",  "Null Pointer: " + getClass().getName() + " > updatePupilName()");
        }
    }

    public long getClassroomId(String classroomName) {

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query
        String[] projection = { COLUMN_CLASSROOM_ID };

        // Define 'where' part of query
        String selection = COLUMN_CLASSROOM_NAME + " LIKE ?";

        // Specify arguments in placeholder order
        String[] selectionArgs = { classroomName };

        // Sorting for results in the resulting Cursor
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        // Run the database query
        if (db != null) {
            Cursor cursor = db.query(
                TABLE_CLASSROOM, // The table to query
                projection,      // The columns to return
                selection,       // The columns for the WHERE clause
                selectionArgs,   // The values for the WHERE clause
                null,            // Don't group the rows
                null,            // Don't filter by row groups
                sortOrder        // The sort order
            );

            // Make sure the cursor is on the id field
            cursor.moveToFirst();

            // Return the id
            try {
                return cursor.getLong(
                        cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_ID));
            } catch (CursorIndexOutOfBoundsException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public ArrayList<Classroom> getClassroomList() {

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Run the database query
        if (db != null) {

            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_CLASSROOM_NAME + " FROM " + TABLE_CLASSROOM,
                    null);

            ArrayList<String> classroomNames = new ArrayList<String>();
            ArrayList<Classroom> classroomArrayList = new ArrayList<Classroom>();

            // Make sure the cursor is on the id field
            cursor.moveToFirst();

            // Iterate over each row to extract classroom names
            for (int i = 0; i < cursor.getCount(); i++) {
                classroomNames.add(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_NAME))
                );

                cursor.moveToNext();
            }

            // Use the classroom names to get pupil list and add as classroom object to array
            for (String classroomName : classroomNames){
                Classroom c = new Classroom(
                        classroomName,
                        getPupils(classroomName));

                classroomArrayList.add(c);
            }

            // Return the id
            return classroomArrayList;
        } else {
            return new ArrayList<Classroom>();
        }
    }

    public String[] getPupils(String classroomName) {

        // Get classroom id
        long classroomId = this.getClassroomId(classroomName);

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query
        String[] projection = { COLUMN_PUPIL_NAME };

        // Define 'where' part of query
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";

        // Specify arguments in placeholder order
        String[] selectionArgs = { String.valueOf(classroomId) };

        // Sorting for results in the resulting Cursor
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        // Run the database query
        if (db != null) {
            Cursor cursor = db.query(
                    TABLE_PUPIL, // The table to query
                    projection,      // The columns to return
                    selection,       // The columns for the WHERE clause
                    selectionArgs,   // The values for the WHERE clause
                    null,            // Don't group the rows
                    null,            // Don't filter by row groups
                    sortOrder        // The sort order
            );

            ArrayList<String> pupilNames = new ArrayList<String>();

            // Make sure the cursor is on the id field
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                pupilNames.add(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_PUPIL_NAME))
                );

                cursor.moveToNext();
            }

            // Return the id
            return pupilNames.toArray(
                    new String[pupilNames.size()]);
        } else {
            return null;
        }
    }

    public boolean sortPupils(String classroomName) {
        // Get classroom id
        long classroomId = this.getClassroomId(classroomName);

        // Puts the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query
        String[] projection = { COLUMN_PUPIL_ID, COLUMN_PUPIL_NAME };

        // Define 'where' part of query
        String selection = COLUMN_CLASSROOM_ID + " LIKE ?";

        // Specify arguments in placeholder order
        String[] selectionArgs = { String.valueOf(classroomId) };

        // Sorting for results in the resulting Cursor
        String sortOrder = COLUMN_CLASSROOM_ID + " DESC";

        // Run the database query
        if (db != null) {
            Cursor cursor = db.query(
                    TABLE_PUPIL, // The table to query
                    projection,      // The columns to return
                    selection,       // The columns for the WHERE clause
                    selectionArgs,   // The values for the WHERE clause
                    null,            // Don't group the rows
                    null,            // Don't filter by row groups
                    sortOrder        // The sort order
            );

            ArrayList<String> originalNames = new ArrayList<String>();
            ArrayList<String> ids = new ArrayList<String>();

            // Make sure the cursor is on the id field
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                originalNames.add(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_PUPIL_NAME))
                );

                ids.add(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_PUPIL_ID))
                );

                cursor.moveToNext();
            }

            // Create a sorted ArrayList
            ArrayList<String> sortedNames = new ArrayList<String>(originalNames);
            Collections.sort(sortedNames);

            // Move cursor back to beginning
            cursor.moveToFirst();

            // Iterate over each row changing new name
            for (int i = 0; i < originalNames.size(); i++) {
                // New value for one column
                ContentValues values = new ContentValues();
                values.put(COLUMN_PUPIL_NAME, sortedNames.get(i));

                // Which row to update, based on the ID
                String sel = COLUMN_PUPIL_ID + " LIKE ?";
                String[] selArgs = { ids.get(i) };

                db.update(TABLE_PUPIL,
                        values,
                        sel,
                        selArgs);
            }

            // Return the id
            return true;
        } else {
            return false;
        }

    }
}