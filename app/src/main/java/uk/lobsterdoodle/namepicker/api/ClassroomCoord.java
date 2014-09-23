package uk.lobsterdoodle.namepicker.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.Util;

/** Created by: Scott Laing
 *  Date: 13-May-2014 @ 23:55 */

  public class ClassroomCoord {

    private static ClassroomCoord mInstance = null;
    private ArrayList<Classroom> mClassrooms;
    private String mCurrentClassroomName;
    private ClassroomDbHelper mDbHelper;
    private SharedPreferences mSharedPrefs;
    private String PREFS_STRING;

    public static ClassroomCoord getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ClassroomCoord(context.getApplicationContext());
        }

        return mInstance;
    }

    private ClassroomCoord (Context context) {
        this.mDbHelper = ClassroomDbHelper.getInstance(context);
        this.mClassrooms = mDbHelper.getClassroomList();
        this.mSharedPrefs = context.getSharedPreferences(Util.FILENAME, 0);
        this.PREFS_STRING = context.getString(R.string.file_current_classroom);
        this.mCurrentClassroomName = mSharedPrefs
                .getString(PREFS_STRING, "Default Class");
        Collections.sort(mClassrooms);

        // If no classrooms exist, add a default classroom
        if (mClassrooms.isEmpty()){
            Classroom cr = new Classroom();
            mClassrooms.add(cr);
            mDbHelper.addClassroom(cr.getName(), cr.getPupils());
        }
    }

    public ArrayList<String> getClassroomNames() {

        // ArrayList for storing classroom names through iterations
        ArrayList<String> classroomNames = new ArrayList<String>();

        // Iterate through classroom, extracting names
        for (Classroom cr : mClassrooms) {
            classroomNames.add(cr.getName());
        }

        // Return ArrayList of names
        return  classroomNames;
    }

    public void setCurrentClassroomName(int index) {
        mCurrentClassroomName = mClassrooms.get(index).getName();

        mSharedPrefs.edit().putString(PREFS_STRING, mCurrentClassroomName).commit();
    }

    public Classroom getCurrentClassroom() {
        return getClassroom(mCurrentClassroomName);
    }

    public int getCurrentClassroomSize() {
        return getCurrentClassroom().getSize();
    }

    public int getCurrentClassroomIndex() {
        return mClassrooms.indexOf(
                getClassroom(mCurrentClassroomName));
    }

    public String getCurrentClassroomName() {
        return mCurrentClassroomName;
    }

    public Pupil getCurrentPupil(int index) {
        return getCurrentClassroom().getPupils().get(index);
    }

    public String getCurrentPupilName(int index) {
        return String.copyValueOf(
                getCurrentPupil(index).getName().toCharArray());
    }

    public ArrayList<String> getCurrentPupils() {
        ArrayList<String> arrayList = new ArrayList<String>();

        for (Pupil pupil : getCurrentClassroom().getPupils()) {
            arrayList.add(pupil.getName());
        }

        return arrayList;
    }

    public int getPosition(String pupilName) {
        return getCurrentClassroom().getPosition(pupilName);
    }

    public Classroom getClassroom(String classroomName) {
        for (Classroom cr : mClassrooms) {
            if (cr.getName().equals(classroomName))
                return cr;
        }
        return null;
    }

    private void addClassroom(Classroom classroom) {
        mClassrooms.add(classroom);
        Collections.sort(mClassrooms);

        mCurrentClassroomName = classroom.getName();

        mDbHelper.addClassroom(mCurrentClassroomName, classroom.getPupils());
        mSharedPrefs.edit().putString(PREFS_STRING, mCurrentClassroomName).commit();
    }

    public void addClassroom(String newClassroomName) {
        Classroom classroom = new Classroom(newClassroomName);
        addClassroom(classroom);
    }

    public void removeClassroom() {

        // Remove from database
        mDbHelper.removeClassroom(mCurrentClassroomName);

        // Remove from classroom list
        mClassrooms.remove(getCurrentClassroom());

        // If list is now empty, add a default classroom
        if (mClassrooms.isEmpty()) addClassroom(new Classroom());

        // Set the current classroom variable
        mCurrentClassroomName = getClassroom(0).getName();
    }

    public Classroom getClassroom(int position) {
        return mClassrooms.get(position);
    }

    public void editClassroomName(String newName) {
        // Change in database
        mDbHelper.updateClassroomName(mCurrentClassroomName, newName);

        //Change the Classroom instance variable
        getClassroom(mCurrentClassroomName).setName(newName);

        // Change current classroom name variable
        mCurrentClassroomName = newName;
    }

    public boolean containsPupil(String pupilName) {
        return getCurrentPupils().contains(pupilName);
    }

    public boolean containsClassroom(String classroomName) {
        return getClassroomNames().contains(classroomName);
    }

    public void addPupil(String pupilName) {
        // Add pupil to current classroom
        getCurrentClassroom().addPupil(pupilName);

        // Add pupil to database
        mDbHelper.addPupil(mCurrentClassroomName, pupilName);
    }

    public void removePupil (String pupilName) {
        // Remove pupil from current classroom
        getCurrentClassroom().removePupil(pupilName);

        // Remove from pupil table in database
        mDbHelper.removePupil(pupilName, mCurrentClassroomName);
    }

    public void changePupilName(String originalName, String newName) {
        // Update in database
        mDbHelper.updatePupilName(originalName, mCurrentClassroomName, newName);

        // Update in classroom array list
        getCurrentClassroom().editPupil(originalName, newName);
    }

    public void sortCurrentPupils() {
        // Sort pupil order in classroom object
        getCurrentClassroom().sortPupils();

        // Sort pupil order in database
        mDbHelper.sortPupils(getCurrentClassroomName());
    }
}
