package uk.lobsterdoodle.namepicker.storage;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDoneSelectedEvent;
import uk.lobsterdoodle.namepicker.edit.EditGroupNamesEvent;
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
    public void on_create_group_done_selected_add_group_to_database() {
        useCase.on(new CreateGroupDoneSelectedEvent("Group Name"));
        verify(dbHelper).createGroup("Group Name");
    }

    @Test
    public void on_create_group_done_selected_event_post_group_creation_successful_event() {
        when(dbHelper.createGroup("Group Name")).thenReturn(24L);
        useCase.on(new CreateGroupDoneSelectedEvent("Group Name"));
        verify(bus).post(new GroupCreationSuccessfulEvent(24L));
    }

    @Test
    public void on_save_group_event_add_group_to_database() {
        useCase.on(new EditGroupNamesEvent(24L, asList("Scott", "Peter")));
        verify(dbHelper).editGroupNames(24L, asList("Scott", "Peter"));
    }

    @Test
    public void on_save_group_event_post_group_saved_successfully_event() {
        useCase.on(new EditGroupNamesEvent(24L, asList("Scott", "Peter")));
        verify(bus).post(new GroupSavedSuccessfullyEvent());
    }

    @Test
    public void on_overview_visible_event_post_overview_retrieved_event() {
        when(dbHelper.getPupils("Group One")).thenReturn(asList("Scott", "Peter"));
        when(dbHelper.getPupils("Group Two")).thenReturn(asList("Rob", "Andy", "Anders", "Rachel", "John"));
        when(dbHelper.getAllGroups()).thenReturn(asList(
                group(1L, "Group One", asList("Scott", "Peter")),
                group(2L, "Group Two", asList("Rob", "Andy"))));

        useCase.on(new OverviewBecameVisibleEvent());

        verify(bus).post(new OverviewRetrievedEvent(asList(
                cellData(1L, "Group One", 2),
                cellData(2L, "Group Two", 5))));
    }

    private OverviewCardCellData cellData(long groupId, String groupName, int nameCount) {
        return new OverviewCardCellData(groupId, groupName, nameCount);
    }

    private Group group(long groupId, String groupName, List<String> nameList) {
        return new Group(groupId, groupName, nameList);
    }
}