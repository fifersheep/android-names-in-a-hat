package uk.lobsterdoodle.namepicker.creategroup;

public class CreateGroupDetailsEvent {

    public final String groupName;

    public CreateGroupDetailsEvent(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateGroupDetailsEvent)) return false;

        CreateGroupDetailsEvent that = (CreateGroupDetailsEvent) o;

        return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;

    }

    @Override
    public int hashCode() {
        return groupName != null ? groupName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CreateGroupDoneSelectedEvent{" +
                "groupName='" + groupName + '\'' +
                '}';
    }
}
