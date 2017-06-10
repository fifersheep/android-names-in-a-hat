package uk.lobsterdoodle.namepicker.selection

import org.greenrobot.eventbus.Subscribe

import uk.lobsterdoodle.namepicker.adapter.AdapterDataWrapper
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.storage.MassNameStateChangedEvent

class SelectionAdapterDataWrapper(private val bus: EventBus) : AdapterDataWrapper<Name>() {
    private var groupId: Long = 0

    init { bus.register(this) }

    fun resume() {
        bus.register(this)
    }

    fun pause() {
        bus.unregister(this)
    }

    fun forGroup(groupId: Long) {
        this.groupId = groupId
    }

    @Subscribe
    fun on(event: GroupNamesSortedEvent) {
        replaceData(event.names)
        bus.post(SelectionDataUpdatedEvent(data()))
        bus.post(if (event.names.isNotEmpty())
            DisableSelectionEmptyStateEvent
        else
            EnableSelectionEmptyStateEvent)
    }

    @Subscribe
    fun on(event: NameSelectionCheckChangedEvent) {
        val name = item(event.position).copyWith(event.isChecked)
        replaceItem(event.position, name)
        bus.post(NameStateChangedEvent(name))
        bus.post(SelectionDataUpdatedEvent(data()))  // todo: necessary? the view already knows it's checked
    }

    @Subscribe
    fun on(event: SelectAllSelectionToggleEvent) {
        val checkedNames = data().filter { it.toggledOn }
        modifyData { name -> name.copyWith(true) }
        bus.post(SelectionDataUpdatedEvent(data()))
        bus.post(MassNameStateChangedEvent.on(groupId))
        bus.post(SelectAllEvent(checkedNames.size, data().size))
    }

    @Subscribe
    fun on(event: ClearAllSelectionToggleEvent) {
        modifyData { name -> name.copyWith(false) }
        bus.post(SelectionDataUpdatedEvent(data()))
        bus.post(MassNameStateChangedEvent.off(groupId))
        bus.post(ClearAllEvent(data().size))
    }
}
