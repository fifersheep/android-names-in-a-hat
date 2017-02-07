package uk.lobsterdoodle.namepicker.selection;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static java.util.Arrays.asList;
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
    public void on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent() {
        when(generator.randomInteger(6)).thenReturn(2);
        when(generator.randomInteger(5)).thenReturn(4);
        useCase.on(new DrawNamesFromSelectionEvent("2", asList("Scott", "Peter", "Rob", "Andy", "Anders", "Rachel")));
        verify(bus).post(new NamesGeneratedEvent(asList("Rob", "Rachel")));
    }
}