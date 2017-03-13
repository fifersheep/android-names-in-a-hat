package uk.lobsterdoodle.namepicker.selection;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class SelectionAdapterDataWrapperTest {

    private SelectionAdapterDataWrapper wrapper;
    private EventBus bus;

    @Before
    public void setUp() {
        bus = mock(EventBus.class);
        wrapper = new SelectionAdapterDataWrapper(bus);
    }

    @Test
    public void registers_on_bus_on_create() {
        verify(bus).register(wrapper);
    }

    @Test
    public void on_GroupNamesRetrievedEvent_store_data() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        assertThat(wrapper.data(), is(equalTo(asList(name("Scott"), name("Suzanne")))));
    }

    @Test
    public void on_GroupNamesRetrievedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_toggle_selection() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        assertThat(wrapper.item(0).toggledOn, is(equalTo(false)));
        wrapper.on(new NameSelectionCheckChangedEvent(0, true));
        assertThat(wrapper.item(0).toggledOn, is(equalTo(true)));
        wrapper.on(new NameSelectionCheckChangedEvent(0, false));
        assertThat(wrapper.item(0).toggledOn, is(equalTo(false)));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        reset(bus);
        wrapper.on(new NameSelectionCheckChangedEvent(0, false));
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_post_NameStateChangedEvent() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott", false), name("Suzanne"))));
        reset(bus);
        wrapper.on(new NameSelectionCheckChangedEvent(0, true));
        verify(bus).post(new NameStateChangedEvent(name("Scott", true)));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_select_all() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        wrapper.on(new SelectAllSelectionToggleEvent());
        assertTrue(allDataToggledOn(wrapper.data()));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott"), name("Suzanne"))));
        reset(bus);
        wrapper.on(new SelectAllSelectionToggleEvent());
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott", true), name("Suzanne", true))));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_clear_all() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott", true), name("Suzanne", true))));
        wrapper.on(new ClearAllSelectionToggleEvent());
        assertTrue(allDataToggledOff(wrapper.data()));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesRetrievedEvent(asList(name("Scott", true), name("Suzanne", true))));
        reset(bus);
        wrapper.on(new ClearAllSelectionToggleEvent());
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    private boolean allDataToggledOn(List<Name> names) {
        return !Iterables.tryFind(names, name -> !name.toggledOn).isPresent();
    }

    private boolean allDataToggledOff(List<Name> names) {
        return !Iterables.tryFind(names, name -> name.toggledOn).isPresent();
    }

    private GroupNamesRetrievedEvent groupNamesRetrievedEvent(List<Name> drawOptions) {
        return new GroupNamesRetrievedEvent(drawOptions);
    }

    private Name name(String name) {
        return new Name(0L, name, false);
    }

    private Name name(String name, boolean toggledOn) {
        return new Name(0L, name, toggledOn);
    }
}