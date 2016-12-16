package uk.lobsterdoodle.namepicker.namelist;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;
import uk.lobsterdoodle.namepicker.addgroup.ShowNamesEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;

import static com.google.common.collect.Lists.transform;

public class NameListUseCase {
    private final EventBus bus;

    @Inject
    public NameListUseCase(EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void onEvent(ShowNamesEvent event) {
        bus.post(new ShowNameCardCellData(transform(event.names, NameCardCellData::new)));
    }
}
