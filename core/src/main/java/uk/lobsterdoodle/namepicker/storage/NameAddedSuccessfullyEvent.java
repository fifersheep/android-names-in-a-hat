package uk.lobsterdoodle.namepicker.storage;

public class NameAddedSuccessfullyEvent {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NameAddedSuccessfullyEvent;
    }
}
