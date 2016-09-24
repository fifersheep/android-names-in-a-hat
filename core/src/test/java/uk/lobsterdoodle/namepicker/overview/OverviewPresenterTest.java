package uk.lobsterdoodle.namepicker.overview;

import org.junit.Before;
import org.junit.Test;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.events.StubbedEventBus;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OverviewPresenterTest {

    private OverviewPresenter presenter;
    private EventBus bus, spy;

    @Before
    public void setUp() {
        bus = new StubbedEventBus();
        spy = spy(bus);
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
}