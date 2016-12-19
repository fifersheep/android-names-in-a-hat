package uk.lobsterdoodle.namepicker.addgroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDoneSelectedEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;

public class AddNamesUseCase {
    private final EventBus bus;
    private final List<String> selectedNames = new ArrayList<>();

    @Inject
    public AddNamesUseCase(EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void onEvent(AddNameSelectedEvent event) {
        selectedNames.add(event.name);
        bus.post(new ShowNamesEvent(selectedNames));
    }

//    @Subscribe
//    public void onEvent(CreateGroupDoneSelectedEvent event) {
//        bus.post(new SaveGroupEvent(event.groupName, selectedNames));
//    }
}
