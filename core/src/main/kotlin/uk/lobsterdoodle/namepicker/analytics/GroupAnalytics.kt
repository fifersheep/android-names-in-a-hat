package uk.lobsterdoodle.namepicker.analytics

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent
import javax.inject.Inject

class GroupAnalytics @Inject constructor(private val bus: EventBus) {

    init { bus.register(this) }

    @Subscribe
    fun on(event: GroupCreationSuccessfulEvent) {
        bus.post(AnalyticsEvent(Key.group_created,
                mapOf(
                        Param.group_id to event.groupId.toString(),
                        Param.group_name to event.groupName)))
    }

    class Key {
        companion object {
            const val group_created: String = "group_created"
        }
    }

    class Param {
        companion object {
            const val group_id: String = "group_id"
            const val group_name: String = "group_name"
        }
    }
}
