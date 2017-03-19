package uk.lobsterdoodle.namepicker.storage;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Name;

public class GroupNamesRetrievedEvent {
    public final long groupId;
    public final List<Name> names;

    public GroupNamesRetrievedEvent(long groupId, List<Name> names) {
        this.groupId = groupId;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupNamesRetrievedEvent)) return false;

        GroupNamesRetrievedEvent that = (GroupNamesRetrievedEvent) o;

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
        return "GroupNamesRetrievedEvent{" +
                "groupId=" + groupId +
                ", names=" + names +
                '}';
    }
}
