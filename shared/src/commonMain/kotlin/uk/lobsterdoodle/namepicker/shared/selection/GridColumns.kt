package uk.lobsterdoodle.namepicker.shared.selection

/** Columns on the draw screen when nothing has been stored. Matches 2.7.0. */
const val DEFAULT_GRID_COLUMNS: Int = 2

/** The draw screen's column count, and the count its toggle switches to. */
data class GridColumns(val current: Int, val next: Int) {
    companion object {
        /** Reads a stored column count; anything other than 1 or 2 reads as [DEFAULT_GRID_COLUMNS]. */
        fun of(stored: Int): GridColumns {
            val current = if (stored == 1 || stored == 2) stored else DEFAULT_GRID_COLUMNS
            return GridColumns(current, next = if (current == 1) 2 else 1)
        }
    }
}
