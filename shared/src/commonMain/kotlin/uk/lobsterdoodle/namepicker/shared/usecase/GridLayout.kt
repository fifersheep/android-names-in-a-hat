package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository
import uk.lobsterdoodle.namepicker.shared.selection.GridColumns

/** The draw screen's column count, shared by every group. */
class GridLayout(private val prefs: PreferencesRepository) {

    fun observe(): Flow<GridColumns> = prefs.gridColumns.map(GridColumns::of)

    suspend fun select(count: Int) = prefs.setGridColumns(count)
}
