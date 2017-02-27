package uk.lobsterdoodle.namepicker.selection;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelectionUseCaseTest {

    private SelectionUseCase useCase;
    private NumberGenerator generator;
    private EventBus bus;

    @Before
    public void setUp() {
        generator = mock(NumberGenerator.class);
        bus = mock(EventBus.class);
        useCase = new SelectionUseCase(generator, bus);
    }

    @Test
    public void registers_on_bus() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_DrawNamesFromSelectionEvent_get_random_int_for_draw_count() {
        useCase.on(new DrawNamesFromSelectionEvent("2", asList("Scott", "Peter", "Rob")));
        verify(generator, times(2)).randomInteger(anyInt());
    }

    @Test
    public void on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent_with_single_name() {
        when(generator.randomInteger(6)).thenReturn(2);
        useCase.on(new DrawNamesFromSelectionEvent("1", asList("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")));
        verify(bus).post(new NamesGeneratedEvent("Rob", false));
    }

    @Test
    public void on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent_with_multiple_names() {
        when(generator.randomInteger(6)).thenReturn(2);
        when(generator.randomInteger(5)).thenReturn(4);
        useCase.on(new DrawNamesFromSelectionEvent("2", asList("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")));
        verify(bus).post(new NamesGeneratedEvent("Rob\nRachel", true));
    }

    @Test
    public void on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_with_some_selected_names() {
        useCase.on(new SelectionDataUpdatedEvent(asList(nameToggledOn(), nameToggledOn(), nameToggledOff(), nameToggledOff())));
        verify(bus).post(new UpdateDrawActionsEvent(asList("1", "2"), "Select All", new SelectAllSelectionToggleEvent()));
    }

    @Test
    public void on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_with_all_selected_names() {
        useCase.on(new SelectionDataUpdatedEvent(asList(nameToggledOn(), nameToggledOn(), nameToggledOn(), nameToggledOn())));
        verify(bus).post(new UpdateDrawActionsEvent(asList("1", "2", "3", "4"), "Clear All", new ClearAllSelectionToggleEvent()));
    }

    @Test
    public void on_SelectionDataUpdatedEvent_post_UpdateDrawActionsEvent_without_selected_names() {
        useCase.on(new SelectionDataUpdatedEvent(asList(nameToggledOff(), nameToggledOff(), nameToggledOff(), nameToggledOff())));
        verify(bus).post(new UpdateDrawActionsEvent(singletonList("0"), "Select All", new SelectAllSelectionToggleEvent()));
    }

    @Test
    public void on_SelectionDataUpdatedEvent_post_DisableDrawActionsEvent_when_1_or_fewer_names_are_toggled_on() {
        useCase.on(new SelectionDataUpdatedEvent(asList(nameToggledOn(), nameToggledOff(), nameToggledOff())));
        verify(bus).post(new DisableDrawActionsEvent());
    }

    @Test
    public void on_SelectionDataUpdatedEvent_post_EnableDrawActionsEvent_when_2_or_more_names_are_toggled_on() {
        useCase.on(new SelectionDataUpdatedEvent(asList(nameToggledOn(), nameToggledOn(), nameToggledOff())));
        verify(bus).post(new EnableDrawActionsEvent());
    }

    private Name nameToggledOn() {
        return new Name(0, "name", true);
    }

    private Name nameToggledOff() {
        return new Name(0, "name", false);
    }
}