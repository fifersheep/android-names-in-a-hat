package uk.lobsterdoodle.namepicker.shared.usecase

/**
 * Validates a group label or a name typed by the user (GitHub #5 and #6).
 *
 * Returns the value with leading and trailing whitespace removed, or null when nothing is
 * left: empty input, or only spaces, tabs or line breaks.
 */
fun validateLabel(raw: String): String? = raw.trim().ifEmpty { null }

/** Tabs and line breaks of every kind, in runs. A name is one line, so each run becomes one space. */
private val LINE_BREAKS_AND_TABS = Regex("[\\t\\n\\u000B\\f\\r\\u0085\\u2028\\u2029]+")

/**
 * Replaces each run of tabs and line breaks in a name with a single space (spec hand-off NAM-9.H1),
 * so a pasted "Ada\nBen" is stored on one line. [validateLabel] only trims the ends.
 */
fun normaliseName(raw: String): String = raw.replace(LINE_BREAKS_AND_TABS, " ")

/** Validates a name: [normaliseName], then [validateLabel]. */
fun validateName(raw: String): String? = validateLabel(normaliseName(raw))

/** The outcome of a write that validates user input first. */
sealed interface WriteResult<out T> {
    /** The input was valid and was saved; [value] is what the write produced. */
    data class Saved<out T>(val value: T) : WriteResult<T>

    /** The input was blank after trimming; nothing was written. */
    data object Blank : WriteResult<Nothing>
}
