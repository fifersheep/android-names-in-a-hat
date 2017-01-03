package uk.lobsterdoodle.namepicker.model;

public class GroupDetails {
    public final long id;
    public final String name;

    public GroupDetails(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupDetails)) return false;

        GroupDetails that = (GroupDetails) o;

        if (id != that.id) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroupDetails{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
