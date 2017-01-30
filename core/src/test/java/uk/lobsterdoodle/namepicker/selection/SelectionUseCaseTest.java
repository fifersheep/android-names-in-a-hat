package uk.lobsterdoodle.namepicker.selection;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
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
    public void on_DrawNamesFromSelectionEvent_get_random_int_for_names_list_size() {
        useCase.on(new DrawNamesFromSelectionEvent(asList("Scott", "Peter")));
        verify(generator).randomInteger(2);
    }

    @Test
    public void on_DrawNamesFromSelectionEvent_post_NamesGeneratedEvent() {
        when(generator.randomInteger(anyInt())).thenReturn(1);
        useCase.on(new DrawNamesFromSelectionEvent(asList("Scott", "Peter")));
        verify(bus).post(new NamesGeneratedEvent(singletonList("Peter")));
    }
}