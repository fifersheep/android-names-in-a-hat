package uk.lobsterdoodle.namepicker.addgroup;

public class AddNameToGroupEvent {
    public final long groupId;
    public final String name;

    public AddNameToGroupEvent(long groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddNameToGroupEvent)) return false;

        AddNameToGroupEvent that = (AddNameToGroupEvent) o;

        if (groupId != that.groupId) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (groupId ^ (groupId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AddNameToGroupEvent{" +
                "groupId=" + groupId +
                ", name='" + name + '\'' +
                '}';
    }
}
