package uk.lobsterdoodle.namepicker.addgroup;

public class AddGroupDoneSelectedEvent {

    public final String groupName;

    public AddGroupDoneSelectedEvent(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddGroupDoneSelectedEvent)) return false;

        AddGroupDoneSelectedEvent that = (AddGroupDoneSelectedEvent) o;

        return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;

    }

    @Override
    public int hashCode() {
        return groupName != null ? groupName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AddGroupDoneSelectedEvent{" +
                "groupName='" + groupName + '\'' +
                '}';
    }
}
