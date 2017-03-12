package uk.lobsterdoodle.namepicker.selection;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.storage.KeyValueStore;

import static java.util.Arrays.asList;

public class SelectionGridUseCase {
    static String CURRENT_GRID_COLUMN_COUNT = "current_grid_column_count";

    private final EventBus bus;
    private final KeyValueStore storage;

    @Inject
    public SelectionGridUseCase(EventBus bus, KeyValueStore storage) {
        this.bus = bus;
        this.storage = storage;
        bus.register(this);
    }

    @Subscribe
    public void on(GridColumnSelectedEvent event) {
        bus.post(new SelectionGridChangedEvent(event.columnCount, nextOptionFor(event.columnCount)));
        storage.edit().put(CURRENT_GRID_COLUMN_COUNT, event.columnCount).commit();
    }

    @Subscribe
    public void on(LoadSelectionGridPreference event) {
        final int storedOption = (int) storage.getLong(CURRENT_GRID_COLUMN_COUNT, 2);
        bus.post(new SelectionGridChangedEvent(storedOption, nextOptionFor(storedOption)));
    }

    private int nextOptionFor(int option) {
        int next = option + 1;
        return asList(1, 2).contains(next) ? next : 1;
    }
}
