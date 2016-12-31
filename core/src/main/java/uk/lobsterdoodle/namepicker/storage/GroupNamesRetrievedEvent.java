package uk.lobsterdoodle.namepicker.storage;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Name;

public class GroupNamesRetrievedEvent {
    public final List<Name> names;

    public GroupNamesRetrievedEvent(List<Name> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupNamesRetrievedEvent)) return false;

        GroupNamesRetrievedEvent that = (GroupNamesRetrievedEvent) o;

        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        return names != null ? names.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupNamesRetrievedEvent{" +
                "names=" + names +
                '}';
    }
}