package uk.lobsterdoodle.namepicker.storage;

public class DeleteGroupEvent {
    public final long groupId;

    public DeleteGroupEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeleteGroupEvent)) return false;

        DeleteGroupEvent that = (DeleteGroupEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "DeleteGroupEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
