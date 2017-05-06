package uk.lobsterdoodle.namepicker.analytics

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.selection.ClearAllEvent
import uk.lobsterdoodle.namepicker.selection.DrawNamesFromSelectionEvent
import uk.lobsterdoodle.namepicker.selection.GridColumnSelectedEvent
import uk.lobsterdoodle.namepicker.selection.SelectAllEvent
import javax.inject.Inject

class SelectionAnalytics @Inject constructor(private val bus: EventBus) {

    init { bus.register(this) }

    @Subscribe
    fun on(event: DrawNamesFromSelectionEvent) {
        bus.post(AnalyticsEvent(Key.selection_names_drawn,
                mapOf(
                        Param.draw_count to event.drawCount,
                        Param.drawn_out_of to event.names.size.toString())))
    }

    @Subscribe
    fun on(event: SelectAllEvent) {
        bus.post(AnalyticsEvent(Key.selection_select_all,
                mapOf(
                        Param.selected_count to event.originalSelectionCount.toString(),
                        Param.selected_out_of to event.totalCount.toString())))
    }

    @Subscribe
    fun on(event: ClearAllEvent) {
        bus.post(AnalyticsEvent(Key.selection_clear_all,
                mapOf(Param.clear_all_count to event.totalCount.toString())))
    }

    @Subscribe
    fun on(event: GridColumnSelectedEvent) {
        bus.post(AnalyticsEvent(Key.selection_grid_change,
                mapOf(Param.grid_column_count to event.columnCount.toString())))
    }

    class Key {
        companion object {
            const val selection_names_drawn: String = "selection_names_drawn"
            const val selection_select_all: String = "selection_select_all"
            const val selection_clear_all: String = "selection_clear_all"
            const val selection_grid_change: String = "selection_grid_change"
        }

    }
    class Param {
        companion object {
            const val draw_count: String = "draw_count"
            const val drawn_out_of: String = "drawn_out_of"
            const val selected_count: String = "selected_count"
            const val selected_out_of: String = "selected_out_of"
            const val clear_all_count: String = "clear_all_count"
            const val grid_column_count: String = "grid_column_count"
        }
    }
}