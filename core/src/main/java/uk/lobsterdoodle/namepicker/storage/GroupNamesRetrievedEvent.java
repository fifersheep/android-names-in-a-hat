package uk.lobsterdoodle.namepicker.storage;

import java.util.List;

public class GroupNamesRetrievedEvent {
    public final List<String> names;

    public GroupNamesRetrievedEvent(List<String> names) {
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
