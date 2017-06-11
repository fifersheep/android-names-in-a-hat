package uk.lobsterdoodle.namepicker.storage

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent
import uk.lobsterdoodle.namepicker.overview.OverviewBecameVisibleEvent
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData
import uk.lobsterdoodle.namepicker.overview.OverviewRetrievedEvent
import uk.lobsterdoodle.namepicker.selection.NameStateChangedEvent
import javax.inject.Inject

class StorageUseCase @Inject
constructor(private val storage: KeyValueStore, private val remoteDb: RemoteDb, private val db: DbHelper, private val bus: EventBus) {

    init {
        bus.register(this)
    }

    @Subscribe
    fun on(event: OverviewBecameVisibleEvent) {
        val groupList = db.allGroups
        val cellData = groupList.map { (details, names) -> OverviewCardCellData(details.id, details.name, names.size) }

        bus.post(OverviewRetrievedEvent(cellData))
        for ((groupId, listTitle, nameCount) in cellData)
            remoteDb.editGroupDetails(groupId, listTitle, nameCount)
    }

    @Subscribe
    fun on(event: CreateGroupDetailsEvent) {
        val groupId = db.createGroup(event.groupName)
        bus.post(GroupCreationSuccessfulEvent(groupId, event.groupName))
    }

    @Subscribe
    fun on(event: AddNameToGroupEvent) {
        db.addNameToGroup(event.groupId, event.name)
        val groupNames = db.retrieveGroupNames(event.groupId)
        bus.post(GroupNamesRetrievedEvent(event.groupId, groupNames))
        bus.post(NameAddedSuccessfullyEvent)
    }

    @Subscribe
    fun on(event: RetrieveGroupNamesEvent) {
        val retrieveGroupNames = db.retrieveGroupNames(event.groupId)
        bus.post(GroupNamesRetrievedEvent(event.groupId, retrieveGroupNames))
    }

    @Subscribe
    fun on(event: DeleteNameEvent) {
        val name = db.removeName(event.nameId)
        name?.let { bus.post(NameDeletedSuccessfullyEvent(name.name)) }
    }

    @Subscribe
    fun on(event: DeleteGroupEvent) {
        val name = db.removeGroup(event.groupId)
        name?.let { bus.post(GroupDeletedSuccessfullyEvent(name.name)) }
    }

    @Subscribe
    fun on(event: EditGroupDetailsEvent) {
        db.editGroupName(event.groupId, event.groupName)
        bus.post(GroupNameEditedSuccessfullyEvent)
    }

    @Subscribe
    fun on(event: RetrieveGroupDetailsEvent) {
        val details = db.retrieveGroupDetails(event.groupId)
        val names = db.retrieveGroupNames(event.groupId)
        details?.let {
            bus.post(GroupDetailsRetrievedSuccessfullyEvent(details))
            remoteDb.editGroupDetails(event.groupId, details.name, names.size)
        }
    }

    @Subscribe
    fun on(event: NameStateChangedEvent) {
        db.updateName(event.name)
    }

    @Subscribe
    fun on(event: MassNameStateChangedEvent) {
        db.toggleAllNamesInGroup(event.groupId, event.toggleOn)
    }
}
