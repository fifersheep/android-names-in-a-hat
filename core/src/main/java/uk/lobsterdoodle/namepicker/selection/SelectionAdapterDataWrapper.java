package uk.lobsterdoodle.namepicker.selection;

import org.greenrobot.eventbus.Subscribe;

import uk.lobsterdoodle.namepicker.adapter.AdapterDataWrapper;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;

public class SelectionAdapterDataWrapper extends AdapterDataWrapper<Name> {

    private final EventBus bus;

    public SelectionAdapterDataWrapper(EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void on(GroupNamesRetrievedEvent event) {
        replaceData(event.names);
        bus.post(new SelectionDataUpdatedEvent(data()));
    }

    @Subscribe
    public void on(NameSelectionCheckChangedEvent event) {
        replaceItem(event.position, item(event.position).copyWith(event.isChecked));
        bus.post(new SelectionDataUpdatedEvent(data()));
    }

    @Subscribe
    public void on(SelectAllSelectionToggleEvent event) {
        modifyData(name -> name.copyWith(true));
        bus.post(new SelectionDataUpdatedEvent(data()));
    }

    @Subscribe
    public void on(ClearAllSelectionToggleEvent event) {
        modifyData(name -> name.copyWith(false));
        bus.post(new SelectionDataUpdatedEvent(data()));
    }
}
