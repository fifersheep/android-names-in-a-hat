package uk.lobsterdoodle.namepicker.selection;

import com.google.common.base.Joiner;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ContiguousSet.create;
import static com.google.common.collect.DiscreteDomain.integers;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Range.closed;
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
        bus.post(new NamesGeneratedEvent(Joiner.on("\n").join(drawnNames), drawnNames.size() > 1));
    }

    @Subscribe
    public void on(SelectionDataUpdatedEvent event) {
        final int checkedNameCount = filter(event.data, n -> n.toggledOn).size();
        final List<String> drawOptions = transform(newArrayList(create(closed(Math.min(1, checkedNameCount), checkedNameCount), integers())), String::valueOf);
        final String toggleLabel = checkedNameCount == event.data.size() ? "Clear All" : "Select All";
        final SelectionToggleEvent toggleClickEvent = checkedNameCount == event.data.size() ? new ClearAllSelectionToggleEvent() : new SelectAllSelectionToggleEvent();
        bus.post(new UpdateDrawActionsEvent(drawOptions, toggleLabel, toggleClickEvent));
    }
}
