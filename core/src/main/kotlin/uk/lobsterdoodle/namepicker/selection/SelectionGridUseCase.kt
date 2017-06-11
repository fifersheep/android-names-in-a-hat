package uk.lobsterdoodle.namepicker.selection

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.storage.KeyValueStore

import java.util.Arrays.asList

class SelectionGridUseCase @Inject
constructor(private val bus: EventBus, private val storage: KeyValueStore) {

    init { bus.register(this) }

    @Subscribe
    fun on(event: GridColumnSelectedEvent) {
        bus.post(SelectionGridChangedEvent(event.columnCount, nextOptionFor(event.columnCount)))
        storage.edit().put(CURRENT_GRID_COLUMN_COUNT, event.columnCount.toLong()).commit()
    }

    @Subscribe
    fun on(event: LoadSelectionGridPreference) {
        val storedOption = storage.getLong(CURRENT_GRID_COLUMN_COUNT, 2).toInt()
        bus.post(SelectionGridChangedEvent(storedOption, nextOptionFor(storedOption)))
    }

    private fun nextOptionFor(option: Int): Int {
        val next = option + 1
        return if (asList(1, 2).contains(next)) next else 1
    }

    companion object {
        internal var CURRENT_GRID_COLUMN_COUNT = "current_grid_column_count"
    }
}
