package uk.lobsterdoodle.namepicker.selection;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;
import uk.lobsterdoodle.namepicker.storage.KeyValueStore;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.lobsterdoodle.namepicker.selection.SelectionNamesUseCase.GROUP_SORT_TYPE_FOR_GROUP_FORMAT;

public class SelectionNamesUseCaseTest {
    private SelectionNamesUseCase useCase;
    private EventBus bus;
    private KeyValueStore store;
    private KeyValueStore.Edit edit;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        store = mock(KeyValueStore.class);
        edit = mock(KeyValueStore.Edit.class);
        when(store.edit()).thenReturn(edit);
        when(edit.put(anyString(), anyString())).thenReturn(edit);
        useCase = new SelectionNamesUseCase(bus, store);
    }

    @Test
    public void registers_on_bus() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_GroupNamesRetrievedEvent_when_sort_type_is_none_post_GroupNamesSortedEvent() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString());
        useCase.on(new GroupNamesRetrievedEvent(24L, asList(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))));
        verify(bus).post(new GroupNamesSortedEvent(asList(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))));
    }

    @Test
    public void on_GroupNamesRetrievedEvent_when_sort_type_is_asc_post_GroupNamesSortedEvent() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.ASC.toString());
        useCase.on(new GroupNamesRetrievedEvent(24L, asList(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))));
        verify(bus).post(new GroupNamesSortedEvent(asList(name(1L, "Bauer"), name(2L, "kim"), name(3L, "Yelena"))));
    }

    @Test
    public void on_GroupNamesRetrievedEvent_when_sort_type_is_desc_post_GroupNamesSortedEvent() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.DESC.toString());
        useCase.on(new GroupNamesRetrievedEvent(24L, asList(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))));
        verify(bus).post(new GroupNamesSortedEvent(asList(name(3L, "Yelena"), name(2L, "kim"), name(1L, "Bauer"))));
    }

    @Test
    public void on_SortMenuItemSelectedEvent_when_sort_type_is_none_write_new_sort_type_asc_to_storage() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString());
        useCase.on(new SortMenuItemSelectedEvent(24L, Collections.emptyList()));
        verify(edit).put("group_sort_type_for_group_24", NameSortType.ASC.toString());
        verify(edit).commit();
    }

    @Test
    public void on_GroupNamesRetrievedEvent_when_sort_type_is_asc_write_new_sort_type_desc_to_storage() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.ASC.toString());
        useCase.on(new SortMenuItemSelectedEvent(24L, Collections.emptyList()));
        verify(edit).put("group_sort_type_for_group_24", NameSortType.DESC.toString());
        verify(edit).commit();
    }

    @Test
    public void on_GroupNamesRetrievedEvent_when_sort_type_is_desc_write_new_sort_type_asc_to_storage() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.DESC.toString());
        useCase.on(new SortMenuItemSelectedEvent(24L, Collections.emptyList()));
        verify(edit).put("group_sort_type_for_group_24", NameSortType.ASC.toString());
        verify(edit).commit();
    }

    @Test
    public void on_SortMenuItemSelectedEvent_post_GroupNamesSortedEvent() {
        when(store.getString(String.format(GROUP_SORT_TYPE_FOR_GROUP_FORMAT, 24L), NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString());
        useCase.on(new SortMenuItemSelectedEvent(24L, asList(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))));
        verify(bus).post(new GroupNamesSortedEvent(asList(name(1L, "Bauer"), name(2L, "kim"), name(3L, "Yelena"))));
    }

    private Name name(long id, String name) {
        return new Name(id, name, false);
    }
}