package uk.lobsterdoodle.namepicker.storage;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Group;
import uk.lobsterdoodle.namepicker.model.GroupDetails;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.overview.OverviewBecameVisibleEvent;
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData;
import uk.lobsterdoodle.namepicker.overview.OverviewRetrievedEvent;
import uk.lobsterdoodle.namepicker.selection.NameStateChangedEvent;

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
    public void on_CreateGroupDetailsEvent_add_group_to_database() {
        useCase.on(new CreateGroupDetailsEvent("Group Name"));
        verify(dbHelper).createGroup("Group Name");
    }

    @Test
    public void on_CreateGroupDetailsEvent_post_GroupCreationSuccessfulEvent() {
        when(dbHelper.createGroup("Group Name")).thenReturn(24L);
        useCase.on(new CreateGroupDetailsEvent("Group Name"));
        verify(bus).post(new GroupCreationSuccessfulEvent(24L));
    }

    @Test
    public void on_AddNameToGroupEvent_save_add_name_to_database() {
        useCase.on(new AddNameToGroupEvent(24L, "Scott"));
        verify(dbHelper).addNameToGroup(24L, "Scott");
    }

    @Test
    public void on_AddNameToGroupEvent_post_GroupNamesRetrievedEvent() {
        when(dbHelper.retrieveGroupNames(24L)).thenReturn(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")));
        useCase.on(new AddNameToGroupEvent(24L, "Scott"));
        verify(bus).post(new GroupNamesRetrievedEvent(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))));
    }

    @Test
    public void on_AddNameToGroupEvent_post_NameAddedSuccessfullyEvent() {
        when(dbHelper.retrieveGroupNames(24L)).thenReturn(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")));
        useCase.on(new AddNameToGroupEvent(24L, "Scott"));
        verify(bus).post(new NameAddedSuccessfullyEvent());
    }

    @Test
    public void on_OverviewBecameVisibleEvent_post_OverviewRetrievedEvent() {
        when(dbHelper.getAllGroups()).thenReturn(asList(
                group(1L, "Group One", asList(name(1L, "Scott"), name(2L, "Peter"))),
                group(2L, "Group Two", asList(name(3L, "Rob"), name(4L, "Andy"), name(5L, "Rachel")))));

        useCase.on(new OverviewBecameVisibleEvent());

        verify(bus).post(new OverviewRetrievedEvent(asList(
                cellData(1L, "Group One", 2),
                cellData(2L, "Group Two", 3))));
    }

    @Test
    public void on_RetrieveGroupNamesEvent_post_GroupNamesRetrievedEvent() {
        when(dbHelper.retrieveGroupNames(24L)).thenReturn(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")));
        useCase.on(new RetrieveGroupNamesEvent(24L));
        verify(bus).post(new GroupNamesRetrievedEvent(asList(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))));
    }

    @Test
    public void on_DeleteNameEvent_remove_name_from_database() {
        when(dbHelper.removeName(24L)).thenReturn(name(3L, "Bauer"));
        useCase.on(new DeleteNameEvent(24L));
        verify(dbHelper).removeName(24L);
        verify(bus).post(new NameDeletedSuccessfullyEvent("Bauer"));
    }

    @Test
    public void on_delete_group_event_remove_group_from_database() {
        when(dbHelper.removeGroup(24L)).thenReturn(details(24L, "CTU"));
        useCase.on(new DeleteGroupEvent(24L));
        verify(dbHelper).removeGroup(24L);
        verify(bus).post(new GroupDeletedSuccessfullyEvent("CTU"));
    }

    @Test
    public void on_EditGroupDetailsEvent_edit_group_name_in_database() {
        useCase.on(new EditGroupDetailsEvent(24L, "CTU"));
        verify(dbHelper).editGroupName(24L, "CTU");
        verify(bus).post(new GroupNameEditedSuccessfullyEvent());
    }

    @Test
    public void on_RetrieveGroupDetailsEvent_post_GroupDetailsRetrievedSuccessfullyEvent_with_details_from_database() {
        when(dbHelper.retrieveGroupDetails(24L)).thenReturn(new GroupDetails(24L, "CTU"));
        useCase.on(new RetrieveGroupDetailsEvent(24L));
        verify(bus).post(new GroupDetailsRetrievedSuccessfullyEvent(new GroupDetails(24L, "CTU")));
    }

    @Test
    public void on_NameStateChangedEvent_update_name_in_database() {
        useCase.on(new NameStateChangedEvent(name(24L, "Jack")));
        verify(dbHelper).updateName(name(24L, "Jack"));
    }

    @Test
    public void on_MassNameStateChangedEvent_mass_toggle_names() {
        useCase.on(MassNameStateChangedEvent.toggleOn(24L));
        verify(dbHelper).toggleAllNamesInGroup(24L, true);
    }

    private OverviewCardCellData cellData(long groupId, String groupName, int nameCount) {
        return new OverviewCardCellData(groupId, groupName, nameCount);
    }

    private GroupDetails details(long id, String name) {
        return new GroupDetails(id, name);
    }

    private Group group(long groupId, String groupName, List<Name> nameList) {
        return new Group(new GroupDetails(groupId, groupName), nameList);
    }

    private Name name(long id, String name) {
        return new Name(id, name, false);
    }
}