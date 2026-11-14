package uk.lobsterdoodle.namepicker.shared.draw

import kotlinx.coroutines.flow.first
import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository

/**
 * Draw from remaining (3.0 spec D8; NAM-12): every draw takes names out of the hat until the list is
 * reset or a name is put back. `selected` and `drawn` stay independent: a reset never changes
 * selection, and select all / clear all never change drawn.
 *
 * Callers serialise these calls (the Draw screen holds one lock for every write), so each reads the
 * names as stored and acts on them.
 */
class HatDraw(private val names: NameRepository, private val nameDraw: NameDraw) {

    /**
     * Draws up to [count] names from the hat (selected and not drawn) and marks them drawn before
     * returning them. If the mark fails, the exception propagates and no result is returned, so a
     * result is never shown that wasn't saved. Nothing in the hat returns an empty list.
     */
    suspend fun draw(groupId: Long, count: Int): List<Name> {
        val picked = nameDraw.drawFrom(names.observeNames(groupId).first(), count)
        if (picked.isNotEmpty()) names.markDrawn(picked.map { it.id })
        return picked
    }

    /**
     * Clears drawn for the whole list without touching selection. Returns the ids that were drawn,
     * for [undoReset]; with nothing drawn it writes nothing and returns an empty list.
     */
    suspend fun reset(groupId: Long): List<Long> {
        val drawn = names.observeNames(groupId).first().filter { it.drawn }.map { it.id }
        if (drawn.isNotEmpty()) names.clearDrawn(groupId)
        return drawn
    }

    /** Undo for [reset]: marks the snapshot drawn again. Ids deleted since are ignored. */
    suspend fun undoReset(ids: Collection<Long>) = names.markDrawn(ids)

    /**
     * "Put back in the hat": the name is selected, then no longer drawn. Selecting first means the
     * chip goes from Drawn straight to In, never showing as Out in between.
     */
    suspend fun putBack(id: Long) {
        names.setSelected(id, true)
        names.clearDrawn(listOf(id))
    }
}
