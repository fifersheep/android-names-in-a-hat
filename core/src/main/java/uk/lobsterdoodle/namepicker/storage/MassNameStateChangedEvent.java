package uk.lobsterdoodle.namepicker.storage;

public class MassNameStateChangedEvent {
    public final long groupId;
    public final boolean toggleOn;

    private MassNameStateChangedEvent(long groupId, boolean toggleOn) {
        this.groupId = groupId;
        this.toggleOn = toggleOn;
    }

    public static MassNameStateChangedEvent toggleOn(long groupId) {
        return new MassNameStateChangedEvent(groupId, true);
    }

    public static MassNameStateChangedEvent toggleOff(long groupId) {
        return new MassNameStateChangedEvent(groupId, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MassNameStateChangedEvent)) return false;

        MassNameStateChangedEvent that = (MassNameStateChangedEvent) o;

        if (groupId != that.groupId) return false;
        return toggleOn == that.toggleOn;

    }

    @Override
    public int hashCode() {
        int result = (int) (groupId ^ (groupId >>> 32));
        result = 31 * result + (toggleOn ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MassNameStateChangedEvent{" +
                "groupId=" + groupId +
                ", toggleOn=" + toggleOn +
                '}';
    }
}
