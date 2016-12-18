package uk.lobsterdoodle.namepicker.storage;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.addgroup.SaveGroupEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Group;
import uk.lobsterdoodle.namepicker.overview.OverviewBecameVisibleEvent;
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData;
import uk.lobsterdoodle.namepicker.overview.OverviewRetrievedEvent;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageUseCaseTest {
    private KeyValueStore storage;
    private DbHelper dbHelper;
    private EventBus bus;
    private StorageUseCase useCase;

    @Before
    public void setUp() {
        storage = mock(KeyValueStore.class);
        dbHelper = mock(DbHelper.class);
        bus = mock(EventBus.class);
        useCase = new StorageUseCase(storage, dbHelper, bus);
    }

    @Test
    public void registers_on_bus_on_creation() {
        verify(bus).register(useCase);
    }

    @Test
    public void on_save_group_event_add_group_to_database() {
        useCase.on(new SaveGroupEvent("Group Name", asList("Scott", "Peter")));
        verify(dbHelper).addClassroom("Group Name", asList("Scott", "Peter"));
    }

    @Test
    public void on_overview_visible_event_post_overview_retrieved_event() {
        when(dbHelper.getPupils("Group One")).thenReturn(asList("Scott", "Peter"));
        when(dbHelper.getPupils("Group Two")).thenReturn(asList("Rob", "Andy", "Anders", "Rachel", "John"));
        when(dbHelper.getClassroomList()).thenReturn(asList(
                group("Group One", asList("Scott", "Peter")),
                group("Group Two", asList("Rob", "Andy"))));

        useCase.on(new OverviewBecameVisibleEvent());

        verify(bus).post(new OverviewRetrievedEvent(asList(
                cellData("Group One", 2),
                cellData("Group Two", 5))));
    }

    private OverviewCardCellData cellData(String groupName, int nameCount) {
        return new OverviewCardCellData(groupName, nameCount);
    }

    private Group group(String groupName, List<String> nameList) {
        return new Group(groupName, nameList);
    }
}