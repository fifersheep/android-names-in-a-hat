package uk.lobsterdoodle.namepicker.api;

/** Created by: Scott Laing
 *  Date: 30-May-2014 @ 11:21 */

  class Pupil implements Comparable<Pupil>{

    private String mName;
    private Classroom mClassroom;

    Pupil (Classroom classroom, String name) {
        // Set name and classroom
        mName = name;
        mClassroom = classroom;
    }

    void setName(String name) { mName = name; }

    String getName() { return mName; }

    void setClassroom(Classroom classroom){ mClassroom = classroom; }

    Classroom getClassroom() { return mClassroom; }

    @Override
    public int compareTo(Pupil p) {
        return mName.compareTo(p.getName());
    }
}
