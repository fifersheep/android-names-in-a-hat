package uk.lobsterdoodle.namepicker.storage;

public class CheckForActiveGroupEvent {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CheckForActiveGroupEvent;
    }
}