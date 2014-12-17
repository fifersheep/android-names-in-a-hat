package uk.lobsterdoodle.namepicker.api;


import org.junit.Test;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;

public class PupilTest {

    String expectedPupilName = "Steve";

    @Test
    public void create_pupil_with_no_classroom_creates_default_class() {
        Pupil pupil = new Pupil(null, expectedPupilName);

        assertNotNull(pupil.getClassroom());
        assertEquals(Classroom.DEFAULT_CLASSROOM_NAME, pupil.getClassroom().getName());
    }

    @Test
    public void creating_a_pupil_with_the_classroom_sets_classroom_field() {
        String expectedClassroomName = "Custom Classroom";
        Classroom classroom = new Classroom(expectedClassroomName);

        Pupil pupil = new Pupil(classroom, expectedPupilName);

        assertEquals(classroom, pupil.getClassroom());
    }

    @Test
    public void creating_a_pupil_sets_the_chosen_pupil_name() {
        Pupil pupil = new Pupil(anyClassroom(), expectedPupilName);

        assertEquals(expectedPupilName, pupil.getName());
    }

    @Test
    public void two_pupils_with_same_name_and_classroom_are_seen_as_identical() {
        Classroom classroomOne = new Classroom("Classroom 1");
        Classroom classroomTwo = new Classroom("Classroom 2");

        Pupil pupilOne = new Pupil(classroomOne, expectedPupilName);
        Pupil pupilTwo = new Pupil(classroomTwo, expectedPupilName);

        assertNotSame(pupilOne, pupilTwo);
    }

    private static Classroom anyClassroom() {
        return mock(Classroom.class);
    }
}