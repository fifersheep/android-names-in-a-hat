package uk.lobsterdoodle.namepicker.storage;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

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

import static com.google.common.collect.Lists.transform;

public class StorageUseCase {
    private final KeyValueStore storage;
    private final RemoteDb remoteDb;
    private final DbHelper db;
    private final EventBus bus;

    @Inject
    public StorageUseCase(KeyValueStore storage, RemoteDb remoteDb, DbHelper db, EventBus bus) {
        this.storage = storage;
        this.remoteDb = remoteDb;
        this.db = db;
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void on(OverviewBecameVisibleEvent event) {
        final List<Group> groupList = db.getAllGroups();
        bus.post(new OverviewRetrievedEvent(transform(groupList,
                group -> new OverviewCardCellData(group.details.id, group.details.name, group.names.size()))));

        if (!storage.getBool("has_uploaded_to_remote", false)) {
            for (Group g : groupList) {
                remoteDb.editGroupDetails(g.details.id, g.details.name);
                for (Name n : g.names) {
                    remoteDb.addName(g.details.id, n.id, n.name);
                }
            }
            storage.edit().put("has_uploaded_to_remote", true);
        }
    }

    @Subscribe
    public void on(CreateGroupDetailsEvent event) {
        final long groupId = db.createGroup(event.groupName);
        remoteDb.editGroupDetails(groupId, event.groupName);
        bus.post(new GroupCreationSuccessfulEvent(groupId, event.groupName));
    }

    @Subscribe
    public void on(AddNameToGroupEvent event) {
        final long nameId = db.addNameToGroup(event.groupId, event.name);
        final List<Name> groupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(event.groupId, groupNames));
        bus.post(new NameAddedSuccessfullyEvent());
        remoteDb.addName(event.groupId, nameId, event.name);
    }

    @Subscribe
    public void on(RetrieveGroupNamesEvent event) {
        final List<Name> retrieveGroupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(event.groupId, retrieveGroupNames));
    }

    @Subscribe
    public void on(DeleteNameEvent event) {
        final Name deletedName = db.removeName(event.getNameId());
        bus.post(new NameDeletedSuccessfullyEvent(deletedName.name));
        remoteDb.deleteName(event.getGroupId(), event.getNameId());
    }

    @Subscribe
    public void on(DeleteGroupEvent event) {
        final GroupDetails details = db.removeGroup(event.groupId);
        remoteDb.removeGroup(event.groupId);
        bus.post(new GroupDeletedSuccessfullyEvent(details.name));
    }

    @Subscribe
    public void on(EditGroupDetailsEvent event) {
        db.editGroupName(event.groupId, event.groupName);
        remoteDb.editGroupDetails(event.groupId, event.groupName);
        bus.post(new GroupNameEditedSuccessfullyEvent());
    }

    @Subscribe
    public void on(RetrieveGroupDetailsEvent event) {
        bus.post(new GroupDetailsRetrievedSuccessfullyEvent(db.retrieveGroupDetails(event.groupId)));
    }

    @Subscribe
    public void on(NameStateChangedEvent event) {
        db.updateName(event.name);
    }

    @Subscribe
    public void on(MassNameStateChangedEvent event) {
        db.toggleAllNamesInGroup(event.groupId, event.toggleOn);
    }
}
