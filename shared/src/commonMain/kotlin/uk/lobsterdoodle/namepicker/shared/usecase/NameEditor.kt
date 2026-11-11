package uk.lobsterdoodle.namepicker.shared.usecase

import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

/** Adds, renames and deletes names and changes which are selected, enforcing the label rules. */
class NameEditor(private val names: NameRepository) {

    /** Adds the trimmed [raw] name to a group, selected (as 2.7.0 did); returns its id, or [WriteResult.Blank]. */
    suspend fun add(groupId: Long, raw: String): WriteResult<Long> {
        val text = validateLabel(raw) ?: return WriteResult.Blank
        return WriteResult.Saved(names.add(groupId, text, selected = true))
    }

    suspend fun rename(id: Long, raw: String): WriteResult<Unit> {
        val text = validateLabel(raw) ?: return WriteResult.Blank
        names.rename(id, text)
        return WriteResult.Saved(Unit)
    }

    /** Deletes a name; returns it, for a "X deleted" message, or null if there was none. */
    suspend fun delete(id: Long): Name? = names.delete(id)

    suspend fun setSelected(id: Long, selected: Boolean) = names.setSelected(id, selected)

    /** Selects every name in the group. Drawn state is left alone. */
    suspend fun selectAll(groupId: Long) = names.setAllSelected(groupId, selected = true)

    /** Deselects every name in the group. Drawn state is left alone. */
    suspend fun clearAll(groupId: Long) = names.setAllSelected(groupId, selected = false)
}
