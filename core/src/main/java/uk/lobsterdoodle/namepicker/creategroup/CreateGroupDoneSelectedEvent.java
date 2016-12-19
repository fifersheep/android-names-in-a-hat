package uk.lobsterdoodle.namepicker.creategroup;

public class CreateGroupDoneSelectedEvent {

    public final String groupName;

    public CreateGroupDoneSelectedEvent(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateGroupDoneSelectedEvent)) return false;

        CreateGroupDoneSelectedEvent that = (CreateGroupDoneSelectedEvent) o;

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
