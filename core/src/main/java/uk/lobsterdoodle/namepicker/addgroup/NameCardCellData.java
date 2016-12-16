package uk.lobsterdoodle.namepicker.addgroup;

public class NameCardCellData {
    public String name;

    public NameCardCellData(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameCardCellData)) return false;

        NameCardCellData that = (NameCardCellData) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NameCardCellData{" +
                "name='" + name + '\'' +
                '}';
    }
}
