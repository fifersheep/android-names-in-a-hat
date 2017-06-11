package uk.lobsterdoodle.namepicker.selection

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.Testing.Util.anyLong
import uk.lobsterdoodle.namepicker.Testing.Util.anyString

import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.storage.KeyValueStore

import uk.lobsterdoodle.namepicker.selection.SelectionGridUseCase.Companion.CURRENT_GRID_COLUMN_COUNT

class SelectionGridUseCaseTest {
    private lateinit var edit: KeyValueStore.Edit
    private lateinit var storage: KeyValueStore
    private lateinit var bus: EventBus
    private lateinit var useCase: SelectionGridUseCase

    @Before
    fun setUp() {
        bus = mock()
        storage = mock()
        edit = mock()
        whenever(storage.edit()).thenReturn(edit)
        whenever(edit.put(anyString(), anyLong())).thenReturn(edit)
        useCase = SelectionGridUseCase(bus, storage)
    }

    @Test
    fun register_on_bus() {
        verify(bus).register(useCase)
    }

    @Test
    fun on_GridColumnSelectedEvent_for_one_post_SelectionGridChangedEvent_for_single_column() {
        useCase.on(GridColumnSelectedEvent(1))
        verify(bus).post(SelectionGridChangedEvent(1, 2))
    }

    @Test
    fun on_GridColumnSelectedEvent_for_two_post_SelectionGridChangedEvent_for_two_columns() {
        useCase.on(GridColumnSelectedEvent(2))
        verify(bus).post(SelectionGridChangedEvent(2, 1))
    }

    @Test
    fun on_GridColumnSelectedEvent_One_write_change_to_storage() {
        useCase.on(GridColumnSelectedEvent(1))
        verify(edit).put(CURRENT_GRID_COLUMN_COUNT, 1)
        verify(edit).commit()
    }

    @Test
    fun on_GridColumnSelectedEvent_Two_write_change_to_storage() {
        useCase.on(GridColumnSelectedEvent(2))
        verify(edit).put(CURRENT_GRID_COLUMN_COUNT, 2)
        verify(edit).commit()
    }

    @Test
    fun on_LoadSelectionGridPreference_post_SelectionGridChangedEvent_with_preference() {
        whenever(storage.getLong(CURRENT_GRID_COLUMN_COUNT, 2)).thenReturn(1L)
        useCase.on(LoadSelectionGridPreference)
        verify(bus).post(SelectionGridChangedEvent(1, 2))
    }
}