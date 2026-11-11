package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.Flow
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** The group that is open, so the app can reopen it on launch. Set on opening, cleared on leaving. */
class ActiveGroup(private val prefs: PreferencesRepository) {

    suspend fun open(groupId: Long) = prefs.setActiveGroup(groupId)

    suspend fun close() = prefs.clearActiveGroup()

    fun observe(): Flow<Long?> = prefs.activeGroupId
}
