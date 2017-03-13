package uk.lobsterdoodle.namepicker.model;

public class Name {
    final public long id;
    final public String name;
    final public boolean toggledOn;

    public Name(long id, String name, boolean toggledOn) {
        this.id = id;
        this.name = name;
        this.toggledOn = toggledOn;
    }

    public Name copyWith(boolean toggledOn) {
        return new Name(id, name, toggledOn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Name)) return false;

        Name n = (Name) o;
        return id == n.id
                && toggledOn == n.toggledOn
                && (name != null ? name.equals(n.name) : n.name == null);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (toggledOn ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Name{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", toggledOn=" + toggledOn +
                '}';
    }
}
