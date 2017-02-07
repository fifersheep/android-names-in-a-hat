package uk.lobsterdoodle.namepicker.selection;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;

public class SelectionUseCase {
    private final NumberGenerator generator;
    private final EventBus bus;

    @Inject
    public SelectionUseCase(NumberGenerator generator, EventBus bus) {
        this.generator = generator;
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void on(DrawNamesFromSelectionEvent event) {
        final List<String> availableNames = newArrayList(event.names);
        final List<String> drawnNames = new ArrayList<>();
        for (int i = 0; i < parseInt(event.drawCount); i++) {
            drawnNames.add(availableNames.remove(generator.randomInteger(availableNames.size())));
        }
        bus.post(new NamesGeneratedEvent(drawnNames));
    }
}
