package uk.lobsterdoodle.namepicker.storage;

public class EditGroupDetailsEvent {
    public final long groupId;
    public final String groupName;

    public EditGroupDetailsEvent(long groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditGroupDetailsEvent)) return false;

        EditGroupDetailsEvent that = (EditGroupDetailsEvent) o;

        if (groupId != that.groupId) return false;
        return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (groupId ^ (groupId >>> 32));
        result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EditGroupDetailsEvent{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
