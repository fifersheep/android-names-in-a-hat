package uk.lobsterdoodle.namepicker.selection;

public class NameSelectionCheckChangedEvent {
    public final int position;
    public final boolean isChecked;

    public NameSelectionCheckChangedEvent(int position, boolean isChecked) {
        this.position = position;
        this.isChecked = isChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameSelectionCheckChangedEvent)) return false;

        NameSelectionCheckChangedEvent that = (NameSelectionCheckChangedEvent) o;

        if (position != that.position) return false;
        return isChecked == that.isChecked;

    }

    @Override
    public int hashCode() {
        int result = position;
        result = 31 * result + (isChecked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NameSelectionCheckChangedEvent{" +
                "position=" + position +
                ", isChecked=" + isChecked +
                '}';
    }
}
