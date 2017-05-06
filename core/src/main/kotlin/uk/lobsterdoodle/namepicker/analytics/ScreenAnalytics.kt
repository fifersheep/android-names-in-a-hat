package uk.lobsterdoodle.namepicker.analytics

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.ui.ScreenLaunchedEvent
import javax.inject.Inject

class ScreenAnalytics @Inject constructor(private val bus: EventBus) {

    init { bus.register(this) }

    @Subscribe
    fun on(event: ScreenLaunchedEvent) {
        bus.post(AnalyticsEvent(Key.screen_launched,
                mapOf(
                        Param.screen_name to event.screenName,
                        Param.screen_orientation to event.orientation)))
    }

    class Key {
        companion object {
            const val screen_launched: String = "screen_launched"
        }
    }

    class Param {
        companion object {
            const val screen_name: String = "screen_name"
            const val screen_orientation: String = "screen_orientation"
        }
    }
}
