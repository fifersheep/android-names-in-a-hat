package uk.lobsterdoodle.namepicker.selection;

import com.google.common.collect.ImmutableMap;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;
import uk.lobsterdoodle.namepicker.storage.KeyValueStore;

public class SelectionNamesUseCase {
    public static String GROUP_SORT_TYPE_FOR_GROUP_FORMAT = "group_sort_type_for_group_%s";

    private final EventBus bus;
    private final KeyValueStore store;

    private Map<NameSortType, NameSortType> nextSortFor = ImmutableMap.<NameSortType, NameSortType>builder()
            .put(NameSortType.NONE, NameSortType.ASC)
            .put(NameSortType.ASC, NameSortType.DESC)
            .put(NameSortType.DESC, NameSortType.ASC)
            .build();

    private Map<NameSortType, Comparator<Name>> comparatorsFor = ImmutableMap.<NameSortType, Comparator<Name>>builder()
            .put(NameSortType.NONE, (first, second) -> 0)
            .put(NameSortType.ASC, (first, second) -> first.name.compareTo(second.name))
            .put(NameSortType.DESC, (first, second) -> second.name.compareTo(first.name))
            .build();

    @Inject
    public SelectionNamesUseCase(EventBus bus, KeyValueStore store) {
        this.bus = bus;
        this.store = store;
        bus.register(this);
    }

    @Subscribe
    public void on(GroupNamesRetrievedEvent event) {
        final List<Name> names = event.names;
        final String key = String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, event.groupId);
        final NameSortType currentSortType = currentSortType(key);

        Collections.sort(names, comparatorsFor.get(currentSortType));
        bus.post(new GroupNamesSortedEvent(names));
    }

    @Subscribe
    public void on(SortMenuItemSelectedEvent event) {
        final List<Name> names = event.names;
        final String key = String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, event.groupId);
        final NameSortType currentSortType = currentSortType(key);
        final NameSortType nextSortType = nextSortFor.get(currentSortType);

        store.edit().put(key, nextSortType.toString()).commit();

        Collections.sort(names, comparatorsFor.get(nextSortType));
        bus.post(new GroupNamesSortedEvent(names));
    }

    private NameSortType currentSortType(String key) {
        return NameSortType.valueOf(store.getString(key, NameSortType.NONE.toString()));
    }
}
