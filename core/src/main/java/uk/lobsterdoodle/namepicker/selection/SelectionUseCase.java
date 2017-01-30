package uk.lobsterdoodle.namepicker.selection;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;

import static java.util.Collections.singletonList;

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
        final int rand = generator.randomInteger(event.names.size());
        bus.post(new NamesGeneratedEvent(singletonList(event.names.get(rand))));
    }
}
