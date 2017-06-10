package uk.lobsterdoodle.namepicker.selection

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.Testing.Util.anyInt

import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent

class SelectionUseCaseTest {

    private lateinit var useCase: SelectionUseCase
    private lateinit var generator: NumberGenerator
    private lateinit var bus: EventBus

    @Before
    fun setUp() {
        generator = mock()
        bus = mock()
        useCase = SelectionUseCase(generator, bus)
    }

    @Test
    fun registers_on_bus() {
        verify(bus).register(useCase)
    }

    @Test
    fun on_DrawNamesFromSelectionEvent_get_random_int_for_draw_count() {
        useCase.on(DrawNamesFromSelectionEvent("2", listOf("Scott", "Peter", "Rob")))
        verify(generator, times(2)).randomInteger(anyInt())
    }

    @Test
    fun on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent_with_single_name() {
        whenever(generator.randomInteger(6)).thenReturn(2)
        useCase.on(DrawNamesFromSelectionEvent("1", listOf("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")))
        verify(bus).post(NamesGeneratedEvent("Rob", false))
    }

    @Test
    fun on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent_with_multiple_names() {
        whenever(generator.randomInteger(6)).thenReturn(2)
        whenever(generator.randomInteger(5)).thenReturn(4)
        useCase.on(DrawNamesFromSelectionEvent("2", listOf("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")))
        verify(bus).post(NamesGeneratedEvent("Rob\nRachel", true))
    }

    @Test
    fun on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_with_some_selected_names() {
        useCase.on(SelectionDataUpdatedEvent(listOf(nameToggledOn(), nameToggledOn(), nameToggledOff(), nameToggledOff())))
        verify(bus).post(UpdateDrawActionsEvent(listOf("1", "2"), "Select All", SelectAllSelectionToggleEvent))
    }

    @Test
    fun on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_with_all_selected_names() {
        useCase.on(SelectionDataUpdatedEvent(listOf(nameToggledOn(), nameToggledOn(), nameToggledOn(), nameToggledOn())))
        verify(bus).post(UpdateDrawActionsEvent(listOf("1", "2", "3", "4"), "Clear All", ClearAllSelectionToggleEvent))
    }

    @Test
    fun on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_without_selected_names() {
        useCase.on(SelectionDataUpdatedEvent(listOf(nameToggledOff(), nameToggledOff(), nameToggledOff(), nameToggledOff())))
        verify(bus).post(UpdateDrawActionsEvent(listOf("0"), "Select All", SelectAllSelectionToggleEvent))
    }

    @Test
    fun on_SelectionDataUpdatedEvent_post_DisableDrawActionsEvent_when_1_or_fewer_names_are_toggled_on() {
        useCase.on(SelectionDataUpdatedEvent(listOf(nameToggledOn(), nameToggledOff(), nameToggledOff())))
        verify(bus).post(DisableDrawActionsEvent)
    }

    @Test
    fun on_SelectionDataUpdatedEvent_post_EnableDrawActionsEvent_when_2_or_more_names_are_toggled_on() {
        useCase.on(SelectionDataUpdatedEvent(listOf(nameToggledOn(), nameToggledOn(), nameToggledOff())))
        verify(bus).post(EnableDrawActionsEvent)
    }

    private fun nameToggledOn(): Name = Name(0, "name", true)
    private fun nameToggledOff(): Name = Name(0, "name", false)
}