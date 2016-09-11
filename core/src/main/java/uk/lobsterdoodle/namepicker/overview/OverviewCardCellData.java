package uk.lobsterdoodle.namepicker.overview;

public class OverviewCardCellData {
    public final String listTitle;
    public final int nameCount;

    public OverviewCardCellData(String listTitle, int nameCount) {
        this.listTitle = listTitle;
        this.nameCount = nameCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverviewCardCellData)) return false;

        OverviewCardCellData that = (OverviewCardCellData) o;

        if (nameCount != that.nameCount) return false;
        return listTitle != null ? listTitle.equals(that.listTitle) : that.listTitle == null;

    }

    @Override
    public int hashCode() {
        int result = listTitle != null ? listTitle.hashCode() : 0;
        result = 31 * result + nameCount;
        return result;
    }

    @Override
    public String toString() {
        return "OverviewCardCellData{" +
                "listTitle='" + listTitle + '\'' +
                ", nameCount=" + nameCount +
                '}';
    }
}
