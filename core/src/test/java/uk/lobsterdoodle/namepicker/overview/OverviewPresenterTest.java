package uk.lobsterdoodle.namepicker.overview;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.events.StubbedEventBus;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OverviewPresenterTest {

    private OverviewView view;
    private OverviewPresenter presenter;
    private EventBus spy;

    @Before
    public void setUp() {
        view = mock(OverviewView.class);
        spy = spy(new StubbedEventBus());
        presenter = new OverviewPresenter(spy);
    }

    @Test
    public void registers_for_events_on_resume() {
        presenter.onResume();
        verify(spy).register(presenter);
    }

    @Test
    public void unregisters_from_events_on_pause() {
        presenter.onPause();
        verify(spy).unregister(presenter);
    }

    @Test
    public void posts_became_visible_event_on_resume() {
        presenter.onResume();
        verify(spy).post(new OverviewBecameVisibleEvent());
    }

    @Test
    public void view_data_set_changed_on_groups_retrieved_event() {
        presenter.onViewCreated(view);
        presenter.onEvent(new OverviewRetrievedEvent(emptyList()));
        verify(view).dataSetChanged();
    }

    @Test
    public void on_overview_received_event_data_is_set() {
        final List<OverviewCardCellData> cellData = new ArrayList<>();
        cellData.add(new OverviewCardCellData("Android Team", 13));
        cellData.add(new OverviewCardCellData("Visitor Services Team", 24));
        presenter.onViewCreated(view);

        presenter.onEvent(new OverviewRetrievedEvent(cellData));

        assertThat(presenter.itemCount(), is(2));
        assertThat(presenter.dataFor(0), is(equalTo(new OverviewCardCellData("Android Team", 13))));
        assertThat(presenter.dataFor(1), is(equalTo(new OverviewCardCellData("Visitor Services Team", 24))));
    }

    @Test
    public void view_show_add_group_dialog_on_add_group_tapped() {
        presenter.onViewCreated(view);
        presenter.addGroupTapped();
        verify(view).launchAddGroupFragment();
    }
}