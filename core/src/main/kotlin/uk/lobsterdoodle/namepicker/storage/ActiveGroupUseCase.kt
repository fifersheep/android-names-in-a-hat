package uk.lobsterdoodle.namepicker.storage

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.events.EventBus

class ActiveGroupUseCase @Inject
constructor(private val bus: EventBus, private val store: KeyValueStore) {

    init {
        bus.register(this)
    }

    @Subscribe
    fun on(event: SetActiveGroupEvent) {
        store.edit().put(KEY, event.groupId).commit()
    }

    @Subscribe
    fun on(event: ClearActiveGroupEvent) {
        store.edit().remove(KEY).commit()
    }

    @Subscribe
    fun on(event: CheckForActiveGroupEvent) {
        bus.post(if (store.contains(KEY))
            GroupActiveEvent(store.getLong(KEY, 0L))
        else
            GroupNotActiveEvent)
    }

    companion object {
        private val KEY = "active_group_id"
    }
}
