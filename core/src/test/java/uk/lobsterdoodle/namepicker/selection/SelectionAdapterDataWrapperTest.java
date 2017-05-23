package uk.lobsterdoodle.namepicker.selection;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.storage.MassNameStateChangedEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
    public void unregister_on_pause() {
        wrapper.pause();
        verify(bus).unregister(wrapper);
    }

    @Test
    public void register_on_resume() {
        reset(bus);
        wrapper.resume();
        verify(bus).register(wrapper);
    }

    @Test
    public void on_GroupNamesRetrievedEvent_store_data() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        assertThat(wrapper.data(), is(equalTo(asList(name("Scott"), name("Suzanne")))));
    }

    @Test
    public void on_GroupNamesRetrievedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_toggle_selection() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        assertThat(wrapper.item(0).getToggledOn(), is(equalTo(false)));
        wrapper.on(new NameSelectionCheckChangedEvent(0, true));
        assertThat(wrapper.item(0).getToggledOn(), is(equalTo(true)));
        wrapper.on(new NameSelectionCheckChangedEvent(0, false));
        assertThat(wrapper.item(0).getToggledOn(), is(equalTo(false)));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        reset(bus);
        wrapper.on(new NameSelectionCheckChangedEvent(0, false));
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    @Test
    public void on_NameSelectionCheckChangedEvent_post_NameStateChangedEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott", false), name("Suzanne"))));
        reset(bus);
        wrapper.on(new NameSelectionCheckChangedEvent(0, true));
        verify(bus).post(new NameStateChangedEvent(name("Scott", true)));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_select_all() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        wrapper.on(new SelectAllSelectionToggleEvent());
        assertTrue(allDataToggledOn(wrapper.data()));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott"), name("Suzanne"))));
        reset(bus);
        wrapper.on(new SelectAllSelectionToggleEvent());
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott", true), name("Suzanne", true))));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_post_SelectAllEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott", true), name("Suzanne", false), name("Jack", true), name("Rena", false))));
        reset(bus);
        wrapper.on(new SelectAllSelectionToggleEvent());
        verify(bus).post(new SelectAllEvent(2, 4));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_clear_all() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott", true), name("Suzanne", true))));
        wrapper.on(new ClearAllSelectionToggleEvent());
        assertTrue(allDataToggledOff(wrapper.data()));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_post_SelectionDataUpdatedEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott", true), name("Suzanne", true))));
        reset(bus);
        wrapper.on(new ClearAllSelectionToggleEvent());
        verify(bus).post(new SelectionDataUpdatedEvent(asList(name("Scott"), name("Suzanne"))));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_post_ClearAllEvent() {
        wrapper.on(groupNamesSortedEvent(asList(name("Scott", true), name("Suzanne", true))));
        reset(bus);
        wrapper.on(new ClearAllSelectionToggleEvent());
        verify(bus).post(new ClearAllEvent(2));
    }

    @Test
    public void on_SelectAllSelectionToggleEvent_post_MassNameStateChangedEvent() {
        wrapper.forGroup(24L);
        wrapper.on(new SelectAllSelectionToggleEvent());
        verify(bus).post(MassNameStateChangedEvent.Toggle.on(24L));
    }

    @Test
    public void on_ClearAllSelectionToggleEvent_post_MassNameStateChangedEvent() {
        wrapper.forGroup(24L);
        wrapper.on(new ClearAllSelectionToggleEvent());
        verify(bus).post(MassNameStateChangedEvent.Toggle.off(24L));
    }

    @Test
    public void on_GroupNamesRetrievedEvent_post_EnableSelectionEmptyStateEvent_when_no_names() {
        wrapper.on(groupNamesSortedEvent(emptyList()));
        verify(bus).post(new EnableSelectionEmptyStateEvent());
    }

    @Test
    public void on_GroupNamesRetrievedEvent_post_DisableSelectionEmptyStateEvent_when_contains_names() {
        wrapper.on(groupNamesSortedEvent(singletonList(name(""))));
        verify(bus).post(new DisableSelectionEmptyStateEvent());
    }

    private boolean allDataToggledOn(List<Name> names) {
        return !Iterables.tryFind(names, name -> !name.getToggledOn()).isPresent();
    }

    private boolean allDataToggledOff(List<Name> names) {
        return !Iterables.tryFind(names, name -> name.getToggledOn()).isPresent();
    }

    private GroupNamesSortedEvent groupNamesSortedEvent(List<Name> drawOptions) {
        return new GroupNamesSortedEvent(drawOptions);
    }

    private Name name(String name) {
        return new Name(0L, name, false);
    }

    private Name name(String name, boolean toggledOn) {
        return new Name(0L, name, toggledOn);
    }
}