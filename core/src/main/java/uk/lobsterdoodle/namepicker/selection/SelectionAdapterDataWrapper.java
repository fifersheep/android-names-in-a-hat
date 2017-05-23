package uk.lobsterdoodle.namepicker.selection;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collection;

import uk.lobsterdoodle.namepicker.adapter.AdapterDataWrapper;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.MassNameStateChangedEvent;

import static com.google.common.collect.Collections2.filter;

public class SelectionAdapterDataWrapper extends AdapterDataWrapper<Name> {

    private final EventBus bus;
    private long groupId;

    public SelectionAdapterDataWrapper(EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    public void resume() {
        bus.register(this);
    }

    public void pause() {
        bus.unregister(this);
    }

    public void forGroup(long groupId) {
        this.groupId = groupId;
    }

    @Subscribe
    public void on(GroupNamesSortedEvent event) {
        replaceData(event.getNames());
        bus.post(new SelectionDataUpdatedEvent(data()));
        bus.post(event.getNames().size() > 0
                ? new DisableSelectionEmptyStateEvent()
                : new EnableSelectionEmptyStateEvent());
    }

    @Subscribe
    public void on(NameSelectionCheckChangedEvent event) {
        final Name name = item(event.getPosition()).copyWith(event.isChecked());
        replaceItem(event.getPosition(), name);
        bus.post(new NameStateChangedEvent(name));
        bus.post(new SelectionDataUpdatedEvent(data()));  // todo: necessary? the view already knows it's checked
    }

    @Subscribe
    public void on(SelectAllSelectionToggleEvent event) {
        final Collection<Name> checkedNames = filter(data(), n -> n.getToggledOn());
        modifyData(name -> name.copyWith(true));
        bus.post(new SelectionDataUpdatedEvent(data()));
        bus.post(MassNameStateChangedEvent.Toggle.on(groupId));
        bus.post(new SelectAllEvent(checkedNames.size(), data().size()));
    }

    @Subscribe
    public void on(ClearAllSelectionToggleEvent event) {
        modifyData(name -> name.copyWith(false));
        bus.post(new SelectionDataUpdatedEvent(data()));
        bus.post(MassNameStateChangedEvent.Toggle.off(groupId));
        bus.post(new ClearAllEvent(data().size()));
    }
}
