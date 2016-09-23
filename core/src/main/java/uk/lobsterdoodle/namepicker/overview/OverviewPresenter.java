package uk.lobsterdoodle.namepicker.overview;

import java.util.ArrayList;
import java.util.List;

public class OverviewPresenter {

    private OverviewView view;
    private List<OverviewCardCellData> cellData = new ArrayList<>();

    public void onViewCreated(OverviewView view) {
        this.view = view;
        cellData.add(new OverviewCardCellData("Position 0", 0));
        cellData.add(new OverviewCardCellData("Position 1", 1));
    }

    public int itemCount() {
        return cellData.size();
    }

    public OverviewCardCellData dataFor(int position) {
        return cellData.get(position);
    }

    public void visibleItems(int firstPosition, int lastPosition) {
        view.dataSetChanged();
    }
}
