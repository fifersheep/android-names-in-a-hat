package uk.lobsterdoodle.namepicker.selection;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.storage.KeyValueStore;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.lobsterdoodle.namepicker.selection.SelectionGridUseCase.CURRENT_GRID_COLUMN_COUNT;

public class SelectionGridUseCaseTest {
    private KeyValueStore.Edit edit;
    private KeyValueStore storage;
    private EventBus bus;
    private SelectionGridUseCase useCase;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        storage = mock(KeyValueStore.class);
        edit = mock(KeyValueStore.Edit.class);
        when(storage.edit()).thenReturn(edit);
        when(edit.put(anyString(), anyLong())).thenReturn(edit);
        useCase = new SelectionGridUseCase(bus, storage);
    }

    @Test
    public void register_on_bus() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_GridColumnSelectedEvent_for_one_post_SelectionGridChangedEvent_for_single_column() {
        useCase.on(new GridColumnSelectedEvent(1));
        verify(bus).post(new SelectionGridChangedEvent(1, 2));
    }

    @Test
    public void on_GridColumnSelectedEvent_for_two_post_SelectionGridChangedEvent_for_two_columns() {
        useCase.on(new GridColumnSelectedEvent(2));
        verify(bus).post(new SelectionGridChangedEvent(2, 1));
    }

    @Test
    public void on_GridColumnSelectedEvent_One_write_change_to_storage() {
        useCase.on(new GridColumnSelectedEvent(1));
        verify(edit).put(CURRENT_GRID_COLUMN_COUNT, 1);
        verify(edit).commit();
    }

    @Test
    public void on_GridColumnSelectedEvent_Two_write_change_to_storage() {
        useCase.on(new GridColumnSelectedEvent(2));
        verify(edit).put(CURRENT_GRID_COLUMN_COUNT, 2);
        verify(edit).commit();
    }

    @Test
    public void on_LoadSelectionGridPreference_post_SelectionGridChangedEvent_with_preference() {
        when(storage.getLong(CURRENT_GRID_COLUMN_COUNT, 2)).thenReturn(1L);
        useCase.on(new LoadSelectionGridPreference());
        verify(bus).post(new SelectionGridChangedEvent(1, 2));
    }
}