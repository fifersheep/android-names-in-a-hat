package uk.lobsterdoodle.namepicker.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The app's destinations (spec "Navigation"). Keys are @Serializable so the back stack, and so each
 * destination's arguments, survive process death. "List" in the UI is `Group` in `:shared`.
 */
@Serializable
sealed interface AppKey : NavKey

/** Home: every list. The only top-level destination. */
@Serializable
data object Lists : AppKey

/** Draw names from one list. Opening it makes the list active. */
@Serializable
data class Draw(val listId: Long) : AppKey

/**
 * Create a list ([listId] null) or edit one.
 *
 * - [isNew]: the list was created during this visit. Done then goes to Draw (rule 2) instead of
 *   back (rule 3). It lives in the key so the choice survives process death.
 * - [session]: identifies this visit. It is the entry's content key, so creating the list (which
 *   swaps the key) keeps the entry, its ViewModel and its saved state. Two different visits never
 *   share them.
 */
@Serializable
data class EditList(val listId: Long?, val isNew: Boolean = false, val session: Long) : AppKey
