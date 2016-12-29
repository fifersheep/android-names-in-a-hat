package uk.lobsterdoodle.namepicker.storage;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDoneSelectedEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Group;
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
        final List<Group> dbClassroomList = db.getAllGroups();
        bus.post(new OverviewRetrievedEvent(transform(dbClassroomList,
                group -> new OverviewCardCellData(group.id, group.name, group.nameList.size()))));
    }

    @Subscribe
    public void on(CreateGroupDoneSelectedEvent event) {
        final long groupId = db.createGroup(event.groupName);
        bus.post(new GroupCreationSuccessfulEvent(groupId));
    }

    @Subscribe
    public void on(AddNameToGroupEvent event) {
        db.addNameToGroup(event.groupId, event.name);
        final List<String> groupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(groupNames));
    }

    @Subscribe
    public void on(RetrieveGroupNamesEvent event) {
        final List<String> retrieveGroupNames = db.retrieveGroupNames(event.groupId);
        bus.post(new GroupNamesRetrievedEvent(retrieveGroupNames));
    }
}
