package uk.lobsterdoodle.namepicker.storage

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test

import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Group
import uk.lobsterdoodle.namepicker.model.GroupDetails
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent
import uk.lobsterdoodle.namepicker.overview.OverviewBecameVisibleEvent
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData
import uk.lobsterdoodle.namepicker.overview.OverviewRetrievedEvent
import uk.lobsterdoodle.namepicker.selection.NameStateChangedEvent

class StorageUseCaseTest {
    private lateinit var storage: KeyValueStore
    private lateinit var dbHelper: DbHelper
    private lateinit var bus: EventBus
    private lateinit var useCase: StorageUseCase
    private lateinit var remoteDb: RemoteDb

    @Before
    fun setUp() {
        storage = mock()
        dbHelper = mock()
        bus = mock()
        remoteDb = mock()
        useCase = StorageUseCase(storage, remoteDb, dbHelper, bus)
    }

    @Test
    fun registers_on_bus_on_creation() {
        verify(bus).register(useCase)
    }

    @Test
    fun on_CreateGroupDetailsEvent_add_group_to_database() {
        useCase.on(CreateGroupDetailsEvent("Group Name"))
        verify(dbHelper).createGroup("Group Name")
    }

    @Test
    fun on_CreateGroupDetailsEvent_post_GroupCreationSuccessfulEvent() {
        whenever(dbHelper.createGroup("Group Name")).thenReturn(24L)
        useCase.on(CreateGroupDetailsEvent("Group Name"))
        verify(bus).post(GroupCreationSuccessfulEvent(24L, "Group Name"))
    }

    @Test
    fun on_AddNameToGroupEvent_save_add_name_to_database() {
        useCase.on(AddNameToGroupEvent(24L, "Scott"))
        verify(dbHelper).addNameToGroup(24L, "Scott")
    }

    @Test
    fun on_AddNameToGroupEvent_post_GroupNamesRetrievedEvent() {
        whenever(dbHelper.retrieveGroupNames(24L)).thenReturn(listOf(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")))
        useCase.on(AddNameToGroupEvent(24L, "Scott"))
        verify(bus).post(GroupNamesRetrievedEvent(24L, listOf(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))))
    }

    @Test
    fun on_AddNameToGroupEvent_post_NameAddedSuccessfullyEvent() {
        whenever(dbHelper.retrieveGroupNames(24L)).thenReturn(listOf(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")))
        useCase.on(AddNameToGroupEvent(24L, "Scott"))
        verify(bus).post(NameAddedSuccessfullyEvent())
    }

    @Test
    fun on_OverviewBecameVisibleEvent_post_OverviewRetrievedEvent() {
        whenever(dbHelper.allGroups).thenReturn(listOf(
                group(1L, "Group One", listOf(name(1L, "Scott"), name(2L, "Peter"))),
                group(2L, "Group Two", listOf(name(3L, "Rob"), name(4L, "Andy"), name(5L, "Rachel")))))

        useCase.on(OverviewBecameVisibleEvent())

        verify(bus).post(OverviewRetrievedEvent(listOf(
                cellData(1L, "Group One", 2),
                cellData(2L, "Group Two", 3))))
    }

    @Test
    fun on_OverviewBecameVisibleEvent_update_remote_db() {
        whenever(dbHelper.allGroups).thenReturn(listOf(
                group(1L, "Group One", listOf(name(11L, "Scott"), name(12L, "Peter"))),
                group(2L, "Group Two", listOf(name(21L, "Rob"), name(22L, "Andy"), name(23L, "Rachel")))))

        useCase.on(OverviewBecameVisibleEvent())

        verify(remoteDb).editGroupDetails(1L, "Group One", 2)
        verify(remoteDb).editGroupDetails(2L, "Group Two", 3)
    }

    @Test
    fun on_RetrieveGroupNamesEvent_post_GroupNamesRetrievedEvent() {
        whenever(dbHelper.retrieveGroupNames(24L)).thenReturn(listOf(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena")))
        useCase.on(RetrieveGroupNamesEvent(24L))
        verify(bus).post(GroupNamesRetrievedEvent(24L, listOf(name(1L, "Bauer"), name(2L, "Kim"), name(3L, "Yelena"))))
    }

    @Test
    fun on_DeleteNameEvent_remove_name_from_database() {
        whenever(dbHelper.removeName(24L)).thenReturn(name(24L, "Bauer"))
        useCase.on(DeleteNameEvent(3L, 24L))
        verify(dbHelper).removeName(24L)
        verify(bus).post(NameDeletedSuccessfullyEvent("Bauer"))
    }

    @Test
    fun on_delete_group_event_remove_group_from_database() {
        whenever(dbHelper.removeGroup(24L)).thenReturn(details(24L, "CTU"))
        useCase.on(DeleteGroupEvent(24L))
        verify(dbHelper).removeGroup(24L)
        verify(bus).post(GroupDeletedSuccessfullyEvent("CTU"))
    }

    @Test
    fun on_EditGroupDetailsEvent_edit_group_name_in_database() {
        useCase.on(EditGroupDetailsEvent(24L, "CTU"))
        verify(dbHelper).editGroupName(24L, "CTU")
        verify(bus).post(GroupNameEditedSuccessfullyEvent())
    }

    @Test
    fun on_RetrieveGroupDetailsEvent_post_GroupDetailsRetrievedSuccessfullyEvent_with_details_from_database() {
        whenever(dbHelper.retrieveGroupDetails(24L)).thenReturn(GroupDetails(24L, "CTU"))
        useCase.on(RetrieveGroupDetailsEvent(24L))
        verify(bus).post(GroupDetailsRetrievedSuccessfullyEvent(GroupDetails(24L, "CTU")))
    }

    @Test
    fun on_RetrieveGroupDetailsEvent_update_remote_db_with_group_details() {
        whenever(dbHelper.retrieveGroupDetails(24L)).thenReturn(GroupDetails(24L, "CTU"))
        whenever(dbHelper.retrieveGroupNames(24L)).thenReturn(listOf(name(1L, "One"), name(2L, "Two")))
        useCase.on(RetrieveGroupDetailsEvent(24L))
        verify(remoteDb).editGroupDetails(24L, "CTU", 2)
    }

    @Test
    fun on_NameStateChangedEvent_update_name_in_database() {
        useCase.on(NameStateChangedEvent(name(24L, "Jack")))
        verify(dbHelper).updateName(name(24L, "Jack"))
    }

    @Test
    fun on_MassNameStateChangedEvent_mass_toggle_names() {
        useCase.on(MassNameStateChangedEvent.Toggle.on(24L))
        verify(dbHelper).toggleAllNamesInGroup(24L, true)
    }

    private fun cellData(groupId: Long, groupName: String, nameCount: Int): OverviewCardCellData =
            OverviewCardCellData(groupId, groupName, nameCount)

    private fun details(id: Long, name: String): GroupDetails =
            GroupDetails(id, name)

    private fun group(groupId: Long, groupName: String, nameList: List<Name>): Group =
            Group(GroupDetails(groupId, groupName), nameList)

    private fun name(id: Long, name: String): Name =
            Name(id, name, false)
}