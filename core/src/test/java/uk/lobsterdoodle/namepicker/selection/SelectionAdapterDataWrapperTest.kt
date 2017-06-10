package uk.lobsterdoodle.namepicker.selection

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.storage.MassNameStateChangedEvent

class SelectionAdapterDataWrapperTest {

    private lateinit var wrapper: SelectionAdapterDataWrapper
    private lateinit var bus: EventBus

    @Before
    fun setUp() {
        bus = mock()
        wrapper = SelectionAdapterDataWrapper(bus)
    }

    @Test
    fun registers_on_bus_on_create() {
        verify(bus).register(wrapper)
    }

    @Test
    fun unregister_on_pause() {
        wrapper.pause()
        verify(bus).unregister(wrapper)
    }

    @Test
    fun register_on_resume() {
        reset(bus)
        wrapper.resume()
        verify(bus).register(wrapper)
    }

    @Test
    fun on_GroupNamesRetrievedEvent_store_data() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        assertThat(wrapper.data(), `is`(equalTo(listOf(name("Scott"), name("Suzanne")))))
    }

    @Test
    fun on_GroupNamesRetrievedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        verify(bus).post(SelectionDataUpdatedEvent(listOf(name("Scott"), name("Suzanne"))))
    }

    @Test
    fun on_NameSelectionCheckChangedEvent_toggle_selection() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        assertThat(wrapper.item(0).toggledOn, `is`(equalTo(false)))
        wrapper.on(NameSelectionCheckChangedEvent(0, true))
        assertThat(wrapper.item(0).toggledOn, `is`(equalTo(true)))
        wrapper.on(NameSelectionCheckChangedEvent(0, false))
        assertThat(wrapper.item(0).toggledOn, `is`(equalTo(false)))
    }

    @Test
    fun on_NameSelectionCheckChangedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        reset(bus)
        wrapper.on(NameSelectionCheckChangedEvent(0, false))
        verify(bus).post(SelectionDataUpdatedEvent(listOf(name("Scott"), name("Suzanne"))))
    }

    @Test
    fun on_NameSelectionCheckChangedEvent_post_NameStateChangedEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott", false), name("Suzanne"))))
        reset(bus)
        wrapper.on(NameSelectionCheckChangedEvent(0, true))
        verify(bus).post(NameStateChangedEvent(name("Scott", true)))
    }

    @Test
    fun on_SelectAllSelectionToggleEvent_select_all() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        wrapper.on(SelectAllSelectionToggleEvent)
        assertTrue(wrapper.data().all { it.toggledOn })
    }

    @Test
    fun on_SelectAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott"), name("Suzanne"))))
        reset(bus)
        wrapper.on(SelectAllSelectionToggleEvent)
        verify(bus).post(SelectionDataUpdatedEvent(listOf(name("Scott", true), name("Suzanne", true))))
    }

    @Test
    fun on_SelectAllSelectionToggleEvent_post_SelectAllEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott", true), name("Suzanne", false), name("Jack", true), name("Rena", false))))
        reset(bus)
        wrapper.on(SelectAllSelectionToggleEvent)
        verify(bus).post(SelectAllEvent(2, 4))
    }

    @Test
    fun on_ClearAllSelectionToggleEvent_clear_all() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott", true), name("Suzanne", true))))
        wrapper.on(ClearAllSelectionToggleEvent)
        assertTrue(wrapper.data().none { it.toggledOn })
    }

    @Test
    fun on_ClearAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott", true), name("Suzanne", true))))
        reset(bus)
        wrapper.on(ClearAllSelectionToggleEvent)
        verify(bus).post(SelectionDataUpdatedEvent(listOf(name("Scott"), name("Suzanne"))))
    }

    @Test
    fun on_ClearAllSelectionToggleEvent_post_ClearAllEvent() {
        wrapper.on(GroupNamesSortedEvent(listOf(name("Scott", true), name("Suzanne", true))))
        reset(bus)
        wrapper.on(ClearAllSelectionToggleEvent)
        verify(bus).post(ClearAllEvent(2))
    }

    @Test
    fun on_SelectAllSelectionToggleEvent_post_MassNameStateChangedEvent() {
        wrapper.forGroup(24L)
        wrapper.on(SelectAllSelectionToggleEvent)
        verify(bus).post(MassNameStateChangedEvent.on(24L))
    }

    @Test
    fun on_ClearAllSelectionToggleEvent_post_MassNameStateChangedEvent() {
        wrapper.forGroup(24L)
        wrapper.on(ClearAllSelectionToggleEvent)
        verify(bus).post(MassNameStateChangedEvent.off(24L))
    }

    @Test
    fun on_GroupNamesRetrievedEvent_post_EnableSelectionEmptyStateEvent_when_no_names() {
        wrapper.on(GroupNamesSortedEvent(emptyList()))
        verify(bus).post(EnableSelectionEmptyStateEvent)
    }

    @Test
    fun on_GroupNamesRetrievedEvent_post_DisableSelectionEmptyStateEvent_when_contains_names() {
        wrapper.on(GroupNamesSortedEvent(listOf(name(""))))
        verify(bus).post(DisableSelectionEmptyStateEvent)
    }

    private fun name(name: String): Name = Name(0L, name, false)

    private fun name(name: String, toggledOn: Boolean): Name = Name(0L, name, toggledOn)
}