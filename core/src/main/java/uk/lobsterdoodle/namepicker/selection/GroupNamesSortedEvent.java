package uk.lobsterdoodle.namepicker.selection;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Name;

public class GroupNamesSortedEvent {
    public final List<Name> names;

    public GroupNamesSortedEvent(List<Name> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupNamesSortedEvent)) return false;

        GroupNamesSortedEvent that = (GroupNamesSortedEvent) o;

        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        return names != null ? names.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupNamesSortedEvent{" +
                "names=" + names +
                '}';
    }
}
