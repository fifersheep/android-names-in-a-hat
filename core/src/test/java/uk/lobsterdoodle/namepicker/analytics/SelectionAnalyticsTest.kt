package uk.lobsterdoodle.namepicker.analytics

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.selection.*

class SelectionAnalyticsTest {
    lateinit var bus: EventBus
    lateinit var analytics: SelectionAnalytics

    @Before
    fun setUp() {
        bus = mock()
        analytics = SelectionAnalytics(bus)
    }

    @Test
    fun registers_on_bus() {
        verify(bus).register(analytics)
    }

    @Test
    fun on_ScreenLaunchedEvent_post_AnalyticsEvent() {
        analytics.on(DrawNamesFromSelectionEvent("2", listOf("a", "b", "c")))
        verify(bus).post(AnalyticsEvent(
                SelectionAnalytics.Key.selection_names_drawn,
                mapOf(
                        SelectionAnalytics.Param.draw_count to "2",
                        SelectionAnalytics.Param.drawn_out_of to "3")))
    }

    @Test
    fun on_SelectAllEvent_post_AnalyticsEvent() {
        analytics.on(SelectAllEvent(2, 4))
        verify(bus).post(AnalyticsEvent(
                SelectionAnalytics.Key.selection_select_all,
                mapOf(
                        SelectionAnalytics.Param.selected_count to "2",
                        SelectionAnalytics.Param.selected_out_of to "4")))
    }

    @Test
    fun on_ClearAllEvent_post_AnalyticsEvent() {
        analytics.on(ClearAllEvent(4))
        verify(bus).post(AnalyticsEvent(
                SelectionAnalytics.Key.selection_clear_all,
                mapOf(SelectionAnalytics.Param.clear_all_count to "4")))
    }

    @Test
    fun on_GridChangedEvent_post_AnalyticsEvent() {
        analytics.on(GridColumnSelectedEvent(3))
        verify(bus).post(AnalyticsEvent(
                SelectionAnalytics.Key.selection_grid_change,
                mapOf(SelectionAnalytics.Param.grid_column_count to "3")))
    }
}