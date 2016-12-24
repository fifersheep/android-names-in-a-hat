package uk.lobsterdoodle.namepicker.edit;

import java.util.List;

public class EditGroupNamesEvent {
    public final long groupId;
    public final List<String> names;

    public EditGroupNamesEvent(long groupId, List<String> names) {
        this.groupId = groupId;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditGroupNamesEvent)) return false;

        EditGroupNamesEvent that = (EditGroupNamesEvent) o;

        if (groupId != that.groupId) return false;
        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (groupId ^ (groupId >>> 32));
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EditGroupNamesEvent{" +
                "groupId=" + groupId +
                ", names=" + names +
                '}';
    }
}
