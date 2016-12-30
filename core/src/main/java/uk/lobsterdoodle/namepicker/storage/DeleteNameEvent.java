package uk.lobsterdoodle.namepicker.storage;

public class DeleteNameEvent {
    public final long id;

    public DeleteNameEvent(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeleteNameEvent)) return false;

        DeleteNameEvent that = (DeleteNameEvent) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "DeleteNameEvent{" +
                "id=" + id +
                '}';
    }
}
