package uk.lobsterdoodle.namepicker.selection;

public class ClearAllSelectionToggleEvent implements SelectionToggleEvent {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClearAllSelectionToggleEvent;
    }
}
