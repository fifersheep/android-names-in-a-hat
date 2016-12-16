package uk.lobsterdoodle.namepicker.addgroup;

import java.util.List;

public class SaveGroupEvent {
    public final String groupName;
    public final List<String> names;

    public SaveGroupEvent(String groupName, List<String> names) {
        this.groupName = groupName;
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaveGroupEvent)) return false;

        SaveGroupEvent that = (SaveGroupEvent) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;
        return names != null ? names.equals(that.names) : that.names == null;

    }

    @Override
    public int hashCode() {
        int result = groupName != null ? groupName.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SaveGroupEvent{" +
                "groupName='" + groupName + '\'' +
                ", names=" + names +
                '}';
    }
}
