package uk.lobsterdoodle.namepicker.model;

import java.util.List;

public class Group {
    public final long id;
    public final String name;
    public final List<Name> nameList;

    public Group(long id, String name, List<Name> nameList) {
        this.id = id;
        this.name = name;
        this.nameList = nameList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (id != group.id) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        return nameList != null ? nameList.equals(group.nameList) : group.nameList == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (nameList != null ? nameList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nameList=" + nameList +
                '}';
    }
}
