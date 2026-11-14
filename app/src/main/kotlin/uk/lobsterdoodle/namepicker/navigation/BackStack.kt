package uk.lobsterdoodle.namepicker.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.random.Random

// Back-stack operations (spec "Navigation" rules 1–6). Lists is always at the bottom. Opening a
// detail truncates to Lists first, so the same operation suits single pane (where Lists is on top
// anyway) and list-detail (where it replaces the detail pane instead of piling entries up).

/** The stack to launch with: the active list over Lists, or just Lists (spec "Launch"). */
fun launchStack(activeListId: Long?): List<AppKey> =
    if (activeListId == null) listOf(Lists) else listOf(Lists, Draw(activeListId))

/** A new [EditList.session]. */
fun newSession(): Long = Random.nextLong()

/** Lists → a list (rule 4). In list-detail it replaces whatever the detail pane shows. */
fun MutableList<NavKey>.openList(id: Long) {
    truncateToLists()
    add(Draw(id))
}

/** Lists → New list (rule 1). */
fun MutableList<NavKey>.newList(session: Long = newSession()) {
    truncateToLists()
    add(EditList(listId = null, session = session))
}

/** Edit list from Draw (pushed over it) or from Lists (replacing the detail) (rule 4). */
fun MutableList<NavKey>.editList(id: Long, fromDraw: Boolean, session: Long = newSession()) {
    if (!fromDraw) truncateToLists()
    add(EditList(listId = id, session = session))
}

/**
 * Visit [session] created its list: swap its `EditList(null)` for `EditList(id)` in place (rule 1).
 * A create that finishes after the user has moved on to another visit changes nothing.
 */
fun MutableList<NavKey>.listCreated(id: Long, session: Long) {
    val top = lastOrNull() as? EditList ?: return
    if (top.listId != null || top.session != session) return
    set(lastIndex, top.copy(listId = id, isNew = true))
}

/** Done on Edit list: a newly created list goes on to Draw (rule 2); otherwise pop (rule 3). */
fun MutableList<NavKey>.doneEditing() {
    val top = lastOrNull() as? EditList ?: return
    if (top.isNew && top.listId != null) set(lastIndex, Draw(top.listId)) else removeAt(lastIndex)
}

/**
 * Pops one entry. Returns the list's id when this was an explicit back-out from Draw to Lists in
 * single pane, the only back that clears the active list (spec "Launch"); otherwise null.
 */
fun MutableList<NavKey>.back(singlePane: Boolean): Long? {
    if (size <= 1) return null
    val popped = removeAt(lastIndex)
    return if (singlePane && popped is Draw && last() == Lists) popped.listId else null
}

/** The list no longer exists: drop every entry for it. */
fun MutableList<NavKey>.listGone(id: Long) {
    removeAll { (it is Draw && it.listId == id) || (it is EditList && it.listId == id) }
}

private fun MutableList<NavKey>.truncateToLists() {
    if (size > 1) subList(1, size).clear()
}
