package uk.lobsterdoodle.namepicker.addgroup;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class EditNamesUseCaseTest {

    private EditNamesUseCase useCase;
    private EventBus bus;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        useCase = new EditNamesUseCase(bus);
    }

    @Test
    public void register_on_creation() {
        verify(bus).register(useCase);
    }

    @Test
    public void post_show_names_event_on_add_name_selected() {
//        useCase.onEvent(new AddNameSelectedEvent("Scott"));
//        verify(bus).post(new ShowNamesEvent(singletonList("Scott")));
//
//        reset(bus);
//        useCase.onEvent(new AddNameSelectedEvent("Peter"));
//        verify(bus).post(new ShowNamesEvent(asList("Scott", "Peter")));
    }
}