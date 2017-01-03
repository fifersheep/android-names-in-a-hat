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

import static com.google.common.collect.Lists.transform;

public class StorageUseCase {
    private final KeyValueStore storage;
    private final DbHelper db;
    private final EventBus bus;

    @Inject
    public StorageUseCase(KeyValueStore storage, DbHelper db, EventBus bus) {
        this.storage = storage;
        this.db = db;
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void on(OverviewBecameVisibleEvent event) {
        final List<Group> groupList = db.getAllGroups();
        bus.post(new OverviewRetrievedEvent(transform(groupList,
                group -> new OverviewCardCellData(group.details.id, group.details.name, group.names.size()))));
    }

    @Subscribe
    public void on(CreateGroupDetailsEvent event) {
        final long groupId = db.createGroup(event.groupName);
        bus.post(new GroupCreationSuccessfulEvent(groupId));
    }

    @Subscribe
    public void on(AddNameToGroupEvent event) {
        db.addNameToGroup(event.groupId, event.name);
        final List<Name> groupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(groupNames));
    }

    @Subscribe
    public void on(RetrieveGroupNamesEvent event) {
        final List<Name> retrieveGroupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(retrieveGroupNames));
    }

    @Subscribe
    public void on(DeleteNameEvent event) {
        final Name deletedName = db.removeName(event.id);
        bus.post(new NameDeletedSuccessfullyEvent(deletedName.name));
    }

    @Subscribe
    public void on(DeleteGroupEvent event) {
        final GroupDetails details = db.removeGroup(event.groupId);
        bus.post(new GroupDeletedSuccessfullyEvent(details.name));
    }

    @Subscribe
    public void on(EditGroupDetailsEvent event) {
        db.editGroupName(event.groupId, event.groupName);
        bus.post(new GroupNameEditedSuccessfullyEvent());
    }

    @Subscribe
    public void on(RetrieveGroupDetailsEvent event) {
        bus.post(new GroupDetailsRetrievedSuccessfullyEvent(db.retrieveGroupDetails(event.groupId)));
    }
}
