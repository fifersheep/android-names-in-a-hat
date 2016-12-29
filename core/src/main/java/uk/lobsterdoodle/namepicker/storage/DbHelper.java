package uk.lobsterdoodle.namepicker.storage;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Group;

public interface DbHelper {
    List<Group> getAllGroups();
    long createGroup(String classroomName);
    void addClassroom (String classroomName, List<String> pupils);
    void editGroupNames (long groupId, List<String> pupils);
    long getClassroomId(String classroomName);
    void updateClassroomName(String originalName, String newName);
    void removeClassroom(String classroomName);

    void addNameToGroup(long groupId, String name);
    void removePupil(String pupilName, String classroomName);
    void updatePupilName(String currentName, String classroomName, String newName);
    List<String> retrieveGroupNames(long groupId);
    boolean sortPupils(String groupId);
}
