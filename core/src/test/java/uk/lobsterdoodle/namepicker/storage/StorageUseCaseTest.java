package uk.lobsterdoodle.namepicker.storage;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDoneSelectedEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Group;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
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
    public void on_add_name_to_group_event_save_add_name_to_database() {
        useCase.on(new AddNameToGroupEvent(24L, "Scott"));
        verify(dbHelper).addNameToGroup(24L, "Scott");
    }

    @Test
    public void on_add_name_to_group_event_post_name_added_successfully_event() {
        when(dbHelper.retrieveGroupNames(24L)).thenReturn(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")));
        useCase.on(new AddNameToGroupEvent(24L, "Scott"));
        verify(bus).post(new GroupNamesRetrievedEvent(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))));
    }

    @Test
    public void on_overview_visible_event_post_overview_retrieved_event() {
        when(dbHelper.getAllGroups()).thenReturn(asList(
                group(1L, "Group One", asList(name(1L, "Scott"), name(2L, "Peter"))),
                group(2L, "Group Two", asList(name(3L, "Rob"), name(4L, "Andy"), name(5L, "Rachel")))));

        useCase.on(new OverviewBecameVisibleEvent());

        verify(bus).post(new OverviewRetrievedEvent(asList(
                cellData(1L, "Group One", 2),
                cellData(2L, "Group Two", 3))));
    }

    @Test
    public void on_retrieve_group_names_event_post_group_names_retrieved_event() {
        when(dbHelper.retrieveGroupNames(24L)).thenReturn(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")));
        useCase.on(new RetrieveGroupNamesEvent(24L));
        verify(bus).post(new GroupNamesRetrievedEvent(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))));
    }

    private OverviewCardCellData cellData(long groupId, String groupName, int nameCount) {
        return new OverviewCardCellData(groupId, groupName, nameCount);
    }

    private Group group(long groupId, String groupName, List<Name> nameList) {
        return new Group(groupId, groupName, nameList);
    }

    private Name name(long id, String name) {
        return new Name(id, name);
    }
}