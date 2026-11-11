package uk.lobsterdoodle.namepicker.shared.usecase

import kotlinx.coroutines.flow.first
import uk.lobsterdoodle.namepicker.shared.model.Group
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository

/** Creates, renames and deletes groups, enforcing the label rules. */
class GroupEditor(
    private val groups: GroupRepository,
    private val prefs: PreferencesRepository,
) {
    /** Creates a group with the trimmed [raw] label; returns its id, or [WriteResult.Blank]. */
    suspend fun create(raw: String): WriteResult<Long> {
        val label = validateLabel(raw) ?: return WriteResult.Blank
        return WriteResult.Saved(groups.create(label))
    }

    suspend fun rename(id: Long, raw: String): WriteResult<Unit> {
        val label = validateLabel(raw) ?: return WriteResult.Blank
        groups.rename(id, label)
        return WriteResult.Saved(Unit)
    }

    /**
     * Deletes a group and its names. When a group was deleted, also forgets its sort order and,
     * if it was the active group, clears that so no dangling id is left.
     * Returns the deleted group, for an "X deleted" message, or null if there was none.
     */
    suspend fun delete(id: Long): Group? {
        val deleted = groups.delete(id) ?: return null
        prefs.removeSortType(id)
        if (prefs.activeGroupId.first() == id) prefs.clearActiveGroup()
        return deleted
    }
}
