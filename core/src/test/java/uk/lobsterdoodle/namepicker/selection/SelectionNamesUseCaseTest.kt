package uk.lobsterdoodle.namepicker.selection

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.Testing.Util.anyString

import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent
import uk.lobsterdoodle.namepicker.storage.KeyValueStore

class SelectionNamesUseCaseTest {
    private lateinit var useCase: SelectionNamesUseCase
    private lateinit var bus: EventBus
    private lateinit var store: KeyValueStore
    private lateinit var edit: KeyValueStore.Edit

    @Before
    fun setUp() {
        bus = mock()
        store = mock()
        edit = mock()
        whenever(store.edit()).thenReturn(edit)
        whenever(edit.put(anyString(), anyString())).thenReturn(edit)
        useCase = SelectionNamesUseCase(bus, store)
    }

    @Test
    fun registers_on_bus() {
        verify(bus).register(useCase)
    }

    @Test
    fun on_GroupNamesRetrievedEvent_when_sort_type_is_none_post_GroupNamesSortedEvent() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString())
        useCase.on(GroupNamesRetrievedEvent(24L, listOf(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))))
        verify(bus).post(GroupNamesSortedEvent(listOf(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))))
    }

    @Test
    fun on_GroupNamesRetrievedEvent_when_sort_type_is_asc_post_GroupNamesSortedEvent() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.ASC.toString())
        useCase.on(GroupNamesRetrievedEvent(24L, listOf(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))))
        verify(bus).post(GroupNamesSortedEvent(listOf(name(1L, "Bauer"), name(2L, "kim"), name(3L, "Yelena"))))
    }

    @Test
    fun on_GroupNamesRetrievedEvent_when_sort_type_is_desc_post_GroupNamesSortedEvent() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.DESC.toString())
        useCase.on(GroupNamesRetrievedEvent(24L, listOf(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))))
        verify(bus).post(GroupNamesSortedEvent(listOf(name(3L, "Yelena"), name(2L, "kim"), name(1L, "Bauer"))))
    }

    @Test
    fun on_SortMenuItemSelectedEvent_when_sort_type_is_none_write_new_sort_type_asc_to_storage() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString())
        useCase.on(SortMenuItemSelectedEvent(24L, emptyList<Name>()))
        verify(edit).put("group_sort_type_for_group_24", NameSortType.ASC.toString())
        verify(edit).commit()
    }

    @Test
    fun on_GroupNamesRetrievedEvent_when_sort_type_is_asc_write_new_sort_type_desc_to_storage() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.ASC.toString())
        useCase.on(SortMenuItemSelectedEvent(24L, emptyList<Name>()))
        verify(edit).put("group_sort_type_for_group_24", NameSortType.DESC.toString())
        verify(edit).commit()
    }

    @Test
    fun on_GroupNamesRetrievedEvent_when_sort_type_is_desc_write_new_sort_type_asc_to_storage() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.DESC.toString())
        useCase.on(SortMenuItemSelectedEvent(24L, emptyList<Name>()))
        verify(edit).put("group_sort_type_for_group_24", NameSortType.ASC.toString())
        verify(edit).commit()
    }

    @Test
    fun on_SortMenuItemSelectedEvent_post_GroupNamesSortedEvent() {
        whenever(store.getString("group_sort_type_for_group_24", NameSortType.NONE.toString())).thenReturn(NameSortType.NONE.toString())
        useCase.on(SortMenuItemSelectedEvent(24L, listOf(name(2L, "kim"), name(1L, "Bauer"), name(3L, "Yelena"))))
        verify(bus).post(GroupNamesSortedEvent(listOf(name(1L, "Bauer"), name(2L, "kim"), name(3L, "Yelena"))))
    }

    private fun name(id: Long, name: String): Name = Name(id, name, false)
}