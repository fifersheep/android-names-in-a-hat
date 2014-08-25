package uk.lobsterdoodle.namepicker.api;

import java.util.ArrayList;
import java.util.Collections;

/** Created by: Scott Laing
 *  Date: 03-Sept-2013 @ 18:16 */

  class Classroom implements Comparable<Classroom>{
    private static String[] DEFAULT_NAMES = {"Amie", "Abbie", "Beth", "Jane", "Joanne", "Louise",
                                                    "Phoebe", "Rachel", "Sam", "Scott", "Tiffany"};
    private String mName;
    private ArrayList<Pupil> mPupils = new ArrayList<Pupil>();

    public Classroom() {
        // Set default name and pupils
        this("Default Class", DEFAULT_NAMES);
    }

    public Classroom(String name) {
        // Set custom name and default pupils
        this(name, DEFAULT_NAMES);
    }

    public Classroom(String name, String[] pupils) {
        // Set classroom name as provided
        setName(name);

        // Set pupils as provided
        setPupils(pupils);
    }

    protected void addPupil(String pupilName) {
        mPupils.add(
                new Pupil(this, pupilName));
    }

    protected void editPupil(String originalName, String newName) {
        Pupil pupil = getPupil(originalName);

        if (pupil != null) {
            pupil.setName(newName);
        }
    }

    protected void removePupil(String pupilName) {
        // Get the targeted pupil
        Pupil pupil = getPupil(pupilName);

        // Remove link to classroom from within pupil
        pupil.setClassroom(null);

        // Remove pupil from list
        if (mPupils.contains(pupil)){
            mPupils.remove(pupil);
        }
    }

    private void setPupils(String[] pupilNames) {
        for (String pupilName : pupilNames) {
            mPupils.add(
                    new Pupil(this, pupilName));
        }
    }

    private Pupil getPupil(String pupilName) {
        Pupil returnValue = null;

        for (Pupil pupil : mPupils) {
            if (pupil.getName().equals(pupilName)) {
                returnValue = pupil;
            }
        }

        return returnValue;
    }

    protected ArrayList<Pupil> getPupils() {
        if (mPupils.isEmpty()) {
            setPupils(DEFAULT_NAMES);
        }
        return mPupils;
    }

    protected void sortPupils() {
        Collections.sort(mPupils);
    }

    protected void setName(String name) {
        mName = name;
    }

    protected  String getName() {
        return mName;
    }

    protected int getSize() {
        return mPupils.size();
    }

    @Override
    public int compareTo(Classroom cr) {
        return mName.compareTo(cr.getName());
    }
}
