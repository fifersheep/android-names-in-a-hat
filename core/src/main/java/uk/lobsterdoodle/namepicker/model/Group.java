package uk.lobsterdoodle.namepicker.model;

import java.util.List;

public class Group {
    public final String name;
    public final List<String> nameList;

    public Group(String name, List<String> nameList) {
        this.name = name;
        this.nameList = nameList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        return nameList != null ? nameList.equals(group.nameList) : group.nameList == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (nameList != null ? nameList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", nameList=" + nameList +
                '}';
    }
}
