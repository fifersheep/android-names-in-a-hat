package uk.lobsterdoodle.namepicker.storage;

public class ClearActiveGroupEvent {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClearActiveGroupEvent;
    }
}
