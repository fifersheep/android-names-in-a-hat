package uk.lobsterdoodle.namepicker.selection;

import java.util.List;

import uk.lobsterdoodle.namepicker.model.Name;

public class SelectionDataUpdatedEvent {

    public final List<Name> data;

    public SelectionDataUpdatedEvent(List<Name> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectionDataUpdatedEvent)) return false;

        SelectionDataUpdatedEvent that = (SelectionDataUpdatedEvent) o;

        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SelectionDataUpdatedEvent{" +
                "data=" + data +
                '}';
    }
}
