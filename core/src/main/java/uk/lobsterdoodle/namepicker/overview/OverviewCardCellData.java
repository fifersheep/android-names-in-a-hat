package uk.lobsterdoodle.namepicker.overview;

public class OverviewCardCellData {
    public final long groupId;
    public final String listTitle;
    public final int nameCount;

    public OverviewCardCellData(long groupId, String listTitle, int nameCount) {
        this.groupId = groupId;
        this.listTitle = listTitle;
        this.nameCount = nameCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverviewCardCellData)) return false;

        OverviewCardCellData that = (OverviewCardCellData) o;

        if (groupId != that.groupId) return false;
        if (nameCount != that.nameCount) return false;
        return listTitle != null ? listTitle.equals(that.listTitle) : that.listTitle == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (groupId ^ (groupId >>> 32));
        result = 31 * result + (listTitle != null ? listTitle.hashCode() : 0);
        result = 31 * result + nameCount;
        return result;
    }

    @Override
    public String toString() {
        return "OverviewCardCellData{" +
                "groupId=" + groupId +
                ", listTitle='" + listTitle + '\'' +
                ", nameCount=" + nameCount +
                '}';
    }
}
