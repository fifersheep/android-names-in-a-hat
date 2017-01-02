package uk.lobsterdoodle.namepicker.storage;

public class RetrieveGroupDetailsEvent {
    public final long groupId;

    public RetrieveGroupDetailsEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetrieveGroupDetailsEvent)) return false;

        RetrieveGroupDetailsEvent that = (RetrieveGroupDetailsEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "RetrieveGroupDetailsEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
