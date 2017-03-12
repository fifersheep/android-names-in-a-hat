package uk.lobsterdoodle.namepicker.selection;

public class GridColumnSelectedEvent {
    public final int columnCount;

    public GridColumnSelectedEvent(int columnCount) {
        this.columnCount = columnCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridColumnSelectedEvent)) return false;

        GridColumnSelectedEvent that = (GridColumnSelectedEvent) o;

        return columnCount == that.columnCount;

    }

    @Override
    public int hashCode() {
        return columnCount;
    }

    @Override
    public String toString() {
        return "GridColumnSelectedEvent{" +
                "columnCount=" + columnCount +
                '}';
    }
}
