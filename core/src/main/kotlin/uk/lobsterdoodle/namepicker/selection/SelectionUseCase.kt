package uk.lobsterdoodle.namepicker.selection

import org.greenrobot.eventbus.Subscribe

import java.util.ArrayList

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent

import java.lang.Integer.parseInt

class SelectionUseCase @Inject
constructor(private val generator: NumberGenerator, private val bus: EventBus) {

    init { bus.register(this) }

    @Subscribe
    fun on(event: DrawNamesFromSelectionEvent) {
        val availableNames = event.names.toMutableList()
        val drawnNames = arrayListOf<String>()
        for (i in 0..parseInt(event.drawCount) - 1) {
            val randomIndex = generator.randomInteger(availableNames.size)
            drawnNames.add(availableNames.removeAt(randomIndex))
        }
        bus.post(NamesGeneratedEvent(drawnNames.joinToString(separator = "\n"), drawnNames.size > 1))
    }

    @Subscribe
    fun on(event: SelectionDataUpdatedEvent) {
        val checkedNameCount = event.data.filter { it.toggledOn }.size
        val range = Math.min(1, checkedNameCount)..checkedNameCount
        val drawOptions: List<String> = range.toList().map { it.toString() }
        val toggleLabel = if (checkedNameCount == event.data.size) "Clear All" else "Select All"
        val toggleClickEvent = if (checkedNameCount == event.data.size) ClearAllSelectionToggleEvent else SelectAllSelectionToggleEvent
        bus.post(UpdateDrawActionsEvent("$checkedNameCount out of ${event.data.size} names selected", drawOptions, toggleLabel, toggleClickEvent))
        bus.post(if (checkedNameCount >= 2) EnableDrawActionsEvent else DisableDrawActionsEvent)
    }
}
