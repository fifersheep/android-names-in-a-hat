package uk.lobsterdoodle.namepicker.overview;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;

public class OverviewUseCase {
    private final EventBus bus;
    private List<OverviewCardCellData> cellData = new ArrayList<>();

    @Inject
    public OverviewUseCase(EventBus bus) {
        this.bus = bus;
        bus.register(this);

        cellData.add(new OverviewCardCellData("The Android Team", 15));
        cellData.add(new OverviewCardCellData("Visitor Services Team", 24));
    }

    @Subscribe
    public void onEvent(OverviewBecameVisibleEvent becameVisibleEvent) {
        bus.post(new OverviewRetrievedEvent(cellData));
    }
}
