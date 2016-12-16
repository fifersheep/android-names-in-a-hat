package uk.lobsterdoodle.namepicker.namelist;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;
import uk.lobsterdoodle.namepicker.addgroup.ShowNamesEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NameListUseCaseTest {

    private EventBus bus;
    private NameListUseCase useCase;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        useCase = new NameListUseCase(bus);
    }

    @Test
    public void registers_to_bus_on_creation() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_show_names_event_post_show_names_cell_data_event() {
        useCase.onEvent(new ShowNamesEvent(asList("Scott", "Peter")));
        verify(bus).post(new ShowNameCardCellData(asList(new NameCardCellData("Scott"), new NameCardCellData("Peter"))));
    }
}