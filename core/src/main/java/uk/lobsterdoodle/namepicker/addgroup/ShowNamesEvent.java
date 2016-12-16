package uk.lobsterdoodle.namepicker.addgroup;

import java.util.List;

public class ShowNamesEvent {
    public final List<String> names;

    public ShowNamesEvent(List<String> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowNamesEvent)) return false;

        ShowNamesEvent that = (ShowNamesEvent) o;

        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        return names != null ? names.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ShowNamesEvent{" +
                "names=" + names +
                '}';
    }
}
