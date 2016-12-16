package uk.lobsterdoodle.namepicker.addgroup;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.events.StubbedEventBus;
import uk.lobsterdoodle.namepicker.ui.DataSetChangedListener;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class NameListPresenterTest {

    private DataSetChangedListener view;
    private NameListPresenter presenter;
    private EventBus spy;

    @Before
    public void setUp() {
        view = mock(DataSetChangedListener.class);
        spy = spy(new StubbedEventBus());
        presenter = new NameListPresenter(spy);
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
        verify(spy).post(new NameListBecameVisibleEvent());
    }

    @Test
    public void view_data_set_changed_on_groups_retrieved_event() {
        presenter.onViewCreated(view);
        presenter.onEvent(new NamesRetrievedEvent(emptyList()));
        verify(view).dataSetChanged();
    }

    @Test
    public void on_overview_received_event_data_is_set() {
        final List<NameCardCellData> cellData = new ArrayList<>();
        cellData.add(new NameCardCellData("Scott"));
        cellData.add(new NameCardCellData("Peter"));
        presenter.onViewCreated(view);

        presenter.onEvent(new NamesRetrievedEvent(cellData));

        assertThat(presenter.itemCount(), is(2));
        assertThat(presenter.dataFor(0), is(equalTo(new NameCardCellData("Scott"))));
        assertThat(presenter.dataFor(1), is(equalTo(new NameCardCellData("Peter"))));
    }
}