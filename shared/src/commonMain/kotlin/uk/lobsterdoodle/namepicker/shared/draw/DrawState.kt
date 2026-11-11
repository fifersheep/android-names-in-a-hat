package uk.lobsterdoodle.namepicker.shared.draw

import uk.lobsterdoodle.namepicker.shared.model.Name

/**
 * The fewest selected names that make a draw available.
 *
 * 2 matches 2.7.0. The design spec (NAM-2) and draw-from-remaining (NAM-12) may lower it to 1.
 */
const val MIN_NAMES_TO_DRAW: Int = 2

/**
 * What the draw controls show for a group's names: the counter, the count options, the
 * select-all/clear-all toggle and whether a draw is available. Wording is left to the UI.
 */
data class DrawState(
    val selectedCount: Int,
    val totalCount: Int,
    /** The counts a user can choose to draw: 1 to [selectedCount]; empty when none are selected. */
    val options: List<Int>,
    /** True when the group has names and every one is selected, so the toggle offers "clear all". */
    val allSelected: Boolean,
    val hasNames: Boolean,
    val canDraw: Boolean,
) {
    companion object {
        fun of(names: List<Name>): DrawState {
            val selected = names.count { it.selected }
            return DrawState(
                selectedCount = selected,
                totalCount = names.size,
                options = (1..selected).toList(),
                allSelected = names.isNotEmpty() && selected == names.size,
                hasNames = names.isNotEmpty(),
                canDraw = selected >= MIN_NAMES_TO_DRAW,
            )
        }
    }
}
