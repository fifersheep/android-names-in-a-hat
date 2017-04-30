package uk.lobsterdoodle.namepicker.storage;

public class SetActiveGroupEvent {
    public final long groupId;

    public SetActiveGroupEvent(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetActiveGroupEvent)) return false;

        SetActiveGroupEvent that = (SetActiveGroupEvent) o;

        return groupId == that.groupId;

    }

    @Override
    public int hashCode() {
        return (int) (groupId ^ (groupId >>> 32));
    }

    @Override
    public String toString() {
        return "SetActiveGroupEvent{" +
                "groupId=" + groupId +
                '}';
    }
}
