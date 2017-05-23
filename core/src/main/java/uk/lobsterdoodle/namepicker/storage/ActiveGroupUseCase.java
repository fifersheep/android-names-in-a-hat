package uk.lobsterdoodle.namepicker.storage;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;

public class ActiveGroupUseCase {
    private static final String KEY = "active_group_id";
    private final EventBus bus;
    private final KeyValueStore store;

    @Inject
    public ActiveGroupUseCase(EventBus bus, KeyValueStore store) {
        this.bus = bus;
        this.store = store;
        bus.register(this);
    }

    @Subscribe
    public void on(SetActiveGroupEvent event) {
        store.edit().put(KEY, event.getGroupId()).commit();
    }

    @Subscribe
    public void on(ClearActiveGroupEvent event) {
        store.edit().remove(KEY).commit();
    }

    @Subscribe
    public void on(CheckForActiveGroupEvent event) {
        bus.post(store.contains(KEY)
                ? new GroupActiveEvent(store.getLong(KEY, 0L))
                : new GroupNotActiveEvent());
    }
}
