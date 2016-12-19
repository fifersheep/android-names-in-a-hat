package uk.lobsterdoodle.namepicker.storage;

public class GroupCreationSuccessfulEvent {
    public final long classroomId;

    public GroupCreationSuccessfulEvent(long classroomId) {
        this.classroomId = classroomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupCreationSuccessfulEvent)) return false;

        GroupCreationSuccessfulEvent that = (GroupCreationSuccessfulEvent) o;

        return classroomId == that.classroomId;

    }

    @Override
    public int hashCode() {
        return (int) (classroomId ^ (classroomId >>> 32));
    }

    @Override
    public String toString() {
        return "GroupCreationSuccessfulEvent{" +
                "classroomId=" + classroomId +
                '}';
    }
}
