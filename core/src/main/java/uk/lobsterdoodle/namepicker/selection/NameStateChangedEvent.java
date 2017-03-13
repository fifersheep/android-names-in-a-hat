package uk.lobsterdoodle.namepicker.selection;

import uk.lobsterdoodle.namepicker.model.Name;

public class NameStateChangedEvent {
    public final Name name;

    public NameStateChangedEvent(Name name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameStateChangedEvent)) return false;

        NameStateChangedEvent that = (NameStateChangedEvent) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NameStateChangedEvent{" +
                "name=" + name +
                '}';
    }
}
