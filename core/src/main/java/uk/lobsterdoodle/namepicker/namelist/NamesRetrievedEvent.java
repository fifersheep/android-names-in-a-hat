package uk.lobsterdoodle.namepicker.namelist;

import java.util.List;

import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;

public class NamesRetrievedEvent {
    public final List<NameCardCellData> cellData;

    public NamesRetrievedEvent(List<NameCardCellData> cellData) {
        this.cellData = cellData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamesRetrievedEvent)) return false;

        NamesRetrievedEvent that = (NamesRetrievedEvent) o;

        return cellData != null ? cellData.equals(that.cellData) : that.cellData == null;

    }

    @Override
    public int hashCode() {
        return cellData != null ? cellData.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NamesRetrievedEvent{" +
                "cellData=" + cellData +
                '}';
    }
}
