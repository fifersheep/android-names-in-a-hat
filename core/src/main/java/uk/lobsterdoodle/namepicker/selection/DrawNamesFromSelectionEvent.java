package uk.lobsterdoodle.namepicker.selection;

import java.util.List;

public class DrawNamesFromSelectionEvent {
    public final List<String> names;

    public DrawNamesFromSelectionEvent(List<String> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawNamesFromSelectionEvent)) return false;

        DrawNamesFromSelectionEvent that = (DrawNamesFromSelectionEvent) o;

        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        return names != null ? names.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DrawNamesFromSelection{" +
                "names=" + names +
                '}';
    }
}
