package uk.lobsterdoodle.namepicker.storage;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.addgroup.SaveGroupEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Group;
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
    public void on(SaveGroupEvent event) {
        db.addClassroom(event.groupName, event.names);
    }

    @Subscribe
    public void on(OverviewBecameVisibleEvent event) {
        final List<Group> dbClassroomList = db.getClassroomList();
        bus.post(new OverviewRetrievedEvent(transform(dbClassroomList, g -> new OverviewCardCellData(g.name, db.getPupils(g.name).size()))));
    }
}
