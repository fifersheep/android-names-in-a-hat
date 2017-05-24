package uk.lobsterdoodle.namepicker.storage

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.Testing.Util.anyLong
import uk.lobsterdoodle.namepicker.Testing.Util.anyString

import uk.lobsterdoodle.namepicker.events.EventBus

class ActiveGroupUseCaseTest {
    private lateinit var bus: EventBus
    private lateinit var store: KeyValueStore
    private lateinit var edit: KeyValueStore.Edit
    private lateinit var useCase: ActiveGroupUseCase

    @Before
    fun setUp() {
        bus = mock()
        store = mock()
        edit = mock()

        whenever(store.edit()).thenReturn(edit)
        whenever(edit.put(anyString(), anyLong())).thenReturn(edit)
        whenever(edit.remove(anyString())).thenReturn(edit)
        useCase = ActiveGroupUseCase(bus, store)
    }

    @Test
    fun register_to_bus() {
        verify(bus).register(useCase)
    }

    @Test
    fun on_SetActiveGroupEvent_write_to_store() {
        useCase.on(SetActiveGroupEvent(24L))
        verify(edit).put("active_group_id", 24L)
        verify(edit).commit()
    }

    @Test
    fun on_ClearActiveGroupEvent_remove_from_store() {
        useCase.on(ClearActiveGroupEvent())
        verify(edit).remove("active_group_id")
        verify(edit).commit()
    }

    @Test
    fun on_CheckForActiveGroupEvent_post_GroupNotActiveEvent_if_store_does_not_contain_key() {
        whenever(store.contains("active_group_id")).thenReturn(false)
        useCase.on(CheckForActiveGroupEvent())
        verify(bus).post(GroupNotActiveEvent())
    }

    @Test
    fun on_CheckForActiveGroupEvent_post_GroupActiveEvent_if_store_contains_key() {
        whenever(store.contains("active_group_id")).thenReturn(true)
        whenever(store.getLong("active_group_id", 0L)).thenReturn(24L)
        useCase.on(CheckForActiveGroupEvent())
        verify(bus).post(GroupActiveEvent(24L))
    }
}