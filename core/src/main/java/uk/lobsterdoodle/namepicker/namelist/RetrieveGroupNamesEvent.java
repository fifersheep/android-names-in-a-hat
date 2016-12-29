package uk.lobsterdoodle.namepicker.namelist;

public class RetrieveGroupNamesEvent {
    public final long groupId;

    public RetrieveGroupNamesEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetrieveGroupNamesEvent)) return false;

        RetrieveGroupNamesEvent that = (RetrieveGroupNamesEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "RetrieveGroupNamesEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
