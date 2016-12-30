package uk.lobsterdoodle.namepicker.storage;

public class NameDeletedSuccessfullyEvent {
    public final String name;

    public NameDeletedSuccessfullyEvent(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameDeletedSuccessfullyEvent)) return false;

        NameDeletedSuccessfullyEvent that = (NameDeletedSuccessfullyEvent) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NameDeletedSuccessfullyEvent{" +
                "name='" + name + '\'' +
                '}';
    }
}
