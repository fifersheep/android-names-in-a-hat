package uk.lobsterdoodle.namepicker.addgroup;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class AddNamesUseCaseTest {

    private AddNamesUseCase useCase;
    private EventBus bus;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        useCase = new AddNamesUseCase(bus);
    }

    @Test
    public void register_on_creation() {
        verify(bus).register(useCase);
    }

    @Test
    public void post_names_retrieved_event_on_add_name_selected() {
        useCase.onEvent(new AddNameSelectedEvent("Scott"));
        verify(bus).post(new ShowNamesEvent(singletonList("Scott")));

        reset(bus);
        useCase.onEvent(new AddNameSelectedEvent("Peter"));
        verify(bus).post(new ShowNamesEvent(asList("Scott", "Peter")));
    }

    @Test
    public void post_save_group_event_on_add_group_selected_with_names() {
        useCase.onEvent(new AddNameSelectedEvent("Scott"));
        useCase.onEvent(new AddNameSelectedEvent("Peter"));
        useCase.onEvent(new AddGroupDoneSelectedEvent("The Cool Gang"));
        verify(bus).post(new SaveGroupEvent("The Cool Gang", asList("Scott", "Peter")));
    }
}