package uk.lobsterdoodle.namepicker.selection;

import java.util.List;

public class DrawNamesFromSelectionEvent {
    public final String drawCount;
    public final List<String> names;

    public DrawNamesFromSelectionEvent(String drawCount, List<String> names) {
        this.drawCount = drawCount;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawNamesFromSelectionEvent)) return false;

        DrawNamesFromSelectionEvent that = (DrawNamesFromSelectionEvent) o;

        if (drawCount != null ? !drawCount.equals(that.drawCount) : that.drawCount != null) return false;
        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        int result = drawCount != null ? drawCount.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DrawNamesFromSelectionEvent{" +
                "drawCount='" + drawCount + '\'' +
                ", names=" + names +
                '}';
    }
}
