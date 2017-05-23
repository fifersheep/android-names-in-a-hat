package uk.lobsterdoodle.namepicker.ui

import uk.lobsterdoodle.namepicker.selection.SelectionToggleEvent

data class UpdateDrawActionsEvent(
        val drawOptions: List<String>,
        val toggleLabel: String,
        val selectionToggleEvent: SelectionToggleEvent
)
