package uk.lobsterdoodle.namepicker.storage;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Group;
import uk.lobsterdoodle.namepicker.model.Name;

public interface DbHelper {
    List<Group> getAllGroups();
    long createGroup(String classroomName);
    void addNameToGroup(long groupId, String name);
    List<Name> retrieveGroupNames(long groupId);
    Name removeName(long nameId);

    void addClassroom (String classroomName, List<String> pupils);
    void editGroupNames (long groupId, List<String> pupils);
    long getClassroomId(String classroomName);
    void updateClassroomName(String originalName, String newName);
    void removeClassroom(String classroomName);
    void updatePupilName(String currentName, String classroomName, String newName);
    boolean sortPupils(String groupId);
}
