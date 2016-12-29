package uk.lobsterdoodle.namepicker.storage;

public class GroupCreationSuccessfulEvent {
    public final long groupId;

    public GroupCreationSuccessfulEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupCreationSuccessfulEvent)) return false;

        GroupCreationSuccessfulEvent that = (GroupCreationSuccessfulEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "GroupCreationSuccessfulEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
