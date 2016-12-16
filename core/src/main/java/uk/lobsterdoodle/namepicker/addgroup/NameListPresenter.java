package uk.lobsterdoodle.namepicker.addgroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.ui.DataSetChangedListener;

public class NameListPresenter {

    private EventBus bus;
    private DataSetChangedListener listener;
    private List<NameCardCellData> cellData = new ArrayList<>();

    @Inject
    public NameListPresenter(EventBus bus) {
        this.bus = bus;
    }

    public void onViewCreated(DataSetChangedListener listener) {
        this.listener = listener;
    }

    public void onResume() {
        bus.register(this);
        bus.post(new NameListBecameVisibleEvent());
    }

    public void onPause() {
        bus.unregister(this);
    }

    public int itemCount() {
        return cellData.size();
    }

    public NameCardCellData dataFor(int position) {
        return cellData.get(position);
    }

    @Subscribe
    public void onEvent(NamesRetrievedEvent event) {
        cellData = event.cellData;
        listener.dataSetChanged();
    }
}
