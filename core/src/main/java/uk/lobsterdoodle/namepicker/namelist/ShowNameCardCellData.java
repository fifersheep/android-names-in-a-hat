package uk.lobsterdoodle.namepicker.namelist;

import java.util.List;

import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;

public class ShowNameCardCellData {
    public final List<NameCardCellData> cellData;

    public ShowNameCardCellData(List<NameCardCellData> cellData) {
        this.cellData = cellData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowNameCardCellData)) return false;

        ShowNameCardCellData that = (ShowNameCardCellData) o;

        return cellData != null ? cellData.equals(that.cellData) : that.cellData == null;

    }

    @Override
    public int hashCode() {
        return cellData != null ? cellData.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ShowNameCardCellData{" +
                "cellData=" + cellData +
                '}';
    }
}
