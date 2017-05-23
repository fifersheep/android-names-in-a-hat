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
        final List<OverviewCardCellData> cellData = transform(groupList,
                group -> new OverviewCardCellData(group.getDetails().getId(), group.getDetails().getName(), group.getNames().size()));

        bus.post(new OverviewRetrievedEvent(cellData));
        for (OverviewCardCellData item : cellData)
            remoteDb.editGroupDetails(item.getGroupId(), item.getListTitle(), item.getNameCount());
    }

    @Subscribe
    public void on(CreateGroupDetailsEvent event) {
        final long groupId = db.createGroup(event.getGroupName());
        bus.post(new GroupCreationSuccessfulEvent(groupId, event.getGroupName()));
    }

    @Subscribe
    public void on(AddNameToGroupEvent event) {
        db.addNameToGroup(event.getGroupId(), event.getName());
        final List<Name> groupNames = db.retrieveGroupNames(event.getGroupId());
        bus.post(new GroupNamesRetrievedEvent(event.getGroupId(), groupNames));
        bus.post(new NameAddedSuccessfullyEvent());
    }

    @Subscribe
    public void on(RetrieveGroupNamesEvent event) {
        final List<Name> retrieveGroupNames = db.retrieveGroupNames(event.getGroupId());
        bus.post(new GroupNamesRetrievedEvent(event.getGroupId(), retrieveGroupNames));
    }

    @Subscribe
    public void on(DeleteNameEvent event) {
        final Name deletedName = db.removeName(event.getNameId());
        bus.post(new NameDeletedSuccessfullyEvent(deletedName.getName()));
    }

    @Subscribe
    public void on(DeleteGroupEvent event) {
        final GroupDetails details = db.removeGroup(event.getGroupId());
        bus.post(new GroupDeletedSuccessfullyEvent(details.getName()));
    }

    @Subscribe
    public void on(EditGroupDetailsEvent event) {
        db.editGroupName(event.getGroupId(), event.getGroupName());
        bus.post(new GroupNameEditedSuccessfullyEvent());
    }

    @Subscribe
    public void on(RetrieveGroupDetailsEvent event) {
        final GroupDetails details = db.retrieveGroupDetails(event.getGroupId());
        final List<Name> names = db.retrieveGroupNames(event.getGroupId());
        bus.post(new GroupDetailsRetrievedSuccessfullyEvent(details));
        remoteDb.editGroupDetails(event.getGroupId(), details.getName(), names.size());
    }

    @Subscribe
    public void on(NameStateChangedEvent event) {
        db.updateName(event.getName());
    }

    @Subscribe
    public void on(MassNameStateChangedEvent event) {
        db.toggleAllNamesInGroup(event.getGroupId(), event.getToggleOn());
    }
}
