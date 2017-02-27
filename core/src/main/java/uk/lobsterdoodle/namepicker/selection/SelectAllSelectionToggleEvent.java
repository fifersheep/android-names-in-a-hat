package uk.lobsterdoodle.namepicker.selection;

public class SelectAllSelectionToggleEvent implements SelectionToggleEvent {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SelectAllSelectionToggleEvent;
    }
}
