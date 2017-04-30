package uk.lobsterdoodle.namepicker.storage;

public class GroupActiveEvent {
    public final long groupId;

    public GroupActiveEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupActiveEvent)) return false;

        GroupActiveEvent that = (GroupActiveEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "GroupActiveEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
