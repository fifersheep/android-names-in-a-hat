package uk.lobsterdoodle.namepicker.model;

import java.util.List;

public class Group {
    public final GroupDetails details;
    public final List<Name> names;

    public Group(GroupDetails details, List<Name> names) {
        this.details = details;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (details != null ? !details.equals(group.details) : group.details != null) return false;
        return names != null ? names.equals(group.names) : group.names == null;

    }

    @Override
    public int hashCode() {
        int result = details != null ? details.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Group{" +
                "details=" + details +
                ", names=" + names +
                '}';
    }
}
