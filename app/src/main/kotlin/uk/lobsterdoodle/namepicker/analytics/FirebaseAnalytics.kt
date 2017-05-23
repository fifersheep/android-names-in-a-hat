package uk.lobsterdoodle.namepicker.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import javax.inject.Inject

class FirebaseAnalytics constructor(val context: Context, bus: EventBus) {
    val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    init { bus.register(this) }

    @Subscribe fun on(event: AnalyticsEvent) {
        val bundle = Bundle()
        event.params.entries.forEach { bundle.putString(it.key, it.value) }
        analytics.logEvent(event.key, bundle)
    }
}
