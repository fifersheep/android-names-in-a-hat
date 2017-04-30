package uk.lobsterdoodle.namepicker.storage;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActiveGroupUseCaseTest {
    private EventBus bus;
    private KeyValueStore store;
    private KeyValueStore.Edit edit;
    private ActiveGroupUseCase useCase;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        store = mock(KeyValueStore.class);
        edit = mock(KeyValueStore.Edit.class);
        when(store.edit()).thenReturn(edit);
        when(edit.put(anyString(), anyLong())).thenReturn(edit);
        when(edit.remove(anyString())).thenReturn(edit);
        useCase = new ActiveGroupUseCase(bus, store);
    }

    @Test
    public void register_to_bus() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_SetActiveGroupEvent_write_to_store() {
        useCase.on(new SetActiveGroupEvent(24L));
        verify(edit).put("active_group_id", 24L);
        verify(edit).commit();
    }

    @Test
    public void on_ClearActiveGroupEvent_remove_from_store() {
        useCase.on(new ClearActiveGroupEvent());
        verify(edit).remove("active_group_id");
        verify(edit).commit();
    }

    @Test
    public void on_CheckForActiveGroupEvent_post_GroupNotActiveEvent_if_store_does_not_contain_key() {
        when(store.contains("active_group_id")).thenReturn(false);
        useCase.on(new CheckForActiveGroupEvent());
        verify(bus).post(new GroupNotActiveEvent());
    }

    @Test
    public void on_CheckForActiveGroupEvent_post_GroupActiveEvent_if_store_contains_key() {
        when(store.contains("active_group_id")).thenReturn(true);
        when(store.getLong("active_group_id", 0L)).thenReturn(24L);
        useCase.on(new CheckForActiveGroupEvent());
        verify(bus).post(new GroupActiveEvent(24L));
    }
}