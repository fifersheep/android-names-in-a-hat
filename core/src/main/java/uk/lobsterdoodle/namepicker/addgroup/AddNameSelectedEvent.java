package uk.lobsterdoodle.namepicker.addgroup;

public class AddNameSelectedEvent {
    public final String name;

    public AddNameSelectedEvent(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddNameSelectedEvent)) return false;

        AddNameSelectedEvent that = (AddNameSelectedEvent) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AddNameSelectedEvent{" +
                "name='" + name + '\'' +
                '}';
    }
}
