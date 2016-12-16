package uk.lobsterdoodle.namepicker.namelist;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;
import uk.lobsterdoodle.namepicker.events.EventBus;

public class NameListUseCase {
    private List<NameCardCellData> cellData = new ArrayList<>();

    private final EventBus bus;

    @Inject
    public NameListUseCase(EventBus bus) {
        this.bus = bus;
        bus.register(this);

        cellData.add(new NameCardCellData("Scott"));
        cellData.add(new NameCardCellData("Peter"));
        cellData.add(new NameCardCellData("Rob"));
        cellData.add(new NameCardCellData("Andy"));
    }

    @Subscribe
    public void onEvent(NameListBecameVisibleEvent becameVisibleEvent) {
        bus.post(new NamesRetrievedEvent(cellData));
    }
}
