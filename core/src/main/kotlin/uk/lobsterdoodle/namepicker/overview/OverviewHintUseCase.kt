package uk.lobsterdoodle.namepicker.overview

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import javax.inject.Inject

class OverviewHintUseCase @Inject constructor(private val bus: EventBus) {
    init {
        bus.register(this)
    }

    @Subscribe
    fun on(event: OverviewRetrievedEvent) {
        if (event.cellData.isEmpty()) bus.post(ShowAddGroupHintEvent)
    }
}