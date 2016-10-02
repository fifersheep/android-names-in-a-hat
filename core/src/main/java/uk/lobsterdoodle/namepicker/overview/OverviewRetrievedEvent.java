package uk.lobsterdoodle.namepicker.overview;

import java.util.List;

public class OverviewRetrievedEvent {
    public final List<OverviewCardCellData> cellData;

    public OverviewRetrievedEvent(List<OverviewCardCellData> cellData) {
        this.cellData = cellData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverviewRetrievedEvent)) return false;

        OverviewRetrievedEvent that = (OverviewRetrievedEvent) o;

        return cellData != null ? cellData.equals(that.cellData) : that.cellData == null;

    }

    @Override
    public int hashCode() {
        return cellData != null ? cellData.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "OverviewRetrievedEvent{" +
                "cellData=" + cellData +
                '}';
    }
}
