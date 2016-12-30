package uk.lobsterdoodle.namepicker.storage;

public class GroupDeletedSuccessfullyEvent {
    public final String groupName;

    public GroupDeletedSuccessfullyEvent(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupDeletedSuccessfullyEvent)) return false;

        GroupDeletedSuccessfullyEvent that = (GroupDeletedSuccessfullyEvent) o;

        return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;

    }

    @Override
    public int hashCode() {
        return groupName != null ? groupName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupDeletedSuccessfullyEvent{" +
                "groupName='" + groupName + '\'' +
                '}';
    }
}
