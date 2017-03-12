package uk.lobsterdoodle.namepicker.selection;

public class SelectionGridChangedEvent {
    public final int gridColumns;
    public final int nextGridOption;

    public SelectionGridChangedEvent(int gridColumns, int nextGridOption) {
        this.gridColumns = gridColumns;
        this.nextGridOption = nextGridOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectionGridChangedEvent)) return false;

        SelectionGridChangedEvent that = (SelectionGridChangedEvent) o;

        if (gridColumns != that.gridColumns) return false;
        return nextGridOption == that.nextGridOption;

    }

    @Override
    public int hashCode() {
        int result = gridColumns;
        result = 31 * result + nextGridOption;
        return result;
    }

    @Override
    public String toString() {
        return "SelectionGridChangedEvent{" +
                "gridColumns=" + gridColumns +
                ", nextGridOption=" + nextGridOption +
                '}';
    }
}
