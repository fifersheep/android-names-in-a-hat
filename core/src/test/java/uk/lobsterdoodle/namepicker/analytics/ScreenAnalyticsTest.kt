package uk.lobsterdoodle.namepicker.analytics

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.ui.ScreenLaunchedEvent

class ScreenAnalyticsTest {
    lateinit var bus: EventBus
    lateinit var analytics: ScreenAnalytics

    @Before
    fun setUp() {
        bus = mock()
        analytics = ScreenAnalytics(bus)
    }

    @Test
    fun registers_on_bus() {
        verify(bus).register(analytics)
    }

    @Test
    fun on_ScreenLaunchedEvent_post_AnalyticsEvent() {
        analytics.on(ScreenLaunchedEvent("Selection Screen", "Portrait"))
        verify(bus).post(AnalyticsEvent(
                ScreenAnalytics.Key.screen_launched,
                mapOf(
                        ScreenAnalytics.Param.screen_name to "Selection Screen",
                        ScreenAnalytics.Param.screen_orientation to "Portrait")))
    }
}