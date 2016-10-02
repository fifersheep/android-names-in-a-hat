package uk.lobsterdoodle.namepicker.overview;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;

public class OverviewPresenter {

    private EventBus bus;
    private OverviewView view;
    private List<OverviewCardCellData> cellData = new ArrayList<>();

    @Inject
    public OverviewPresenter(EventBus bus) {
        this.bus = bus;
    }

    public void onViewCreated(OverviewView view) {
        this.view = view;
    }

    public void onResume() {
        bus.register(this);
        bus.post(new OverviewBecameVisibleEvent());
    }

    public void onPause() {
        bus.unregister(this);
    }

    public int itemCount() {
        return cellData.size();
    }

    public OverviewCardCellData dataFor(int position) {
        return cellData.get(position);
    }

    @Subscribe
    public void onEvent(OverviewRetrievedEvent retrievedEvent) {
        cellData = retrievedEvent.cellData;
        view.dataSetChanged();
    }
}
