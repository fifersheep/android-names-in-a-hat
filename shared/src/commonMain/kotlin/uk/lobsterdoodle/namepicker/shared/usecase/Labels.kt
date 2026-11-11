package uk.lobsterdoodle.namepicker.shared.usecase

/**
 * Validates a group label or a name typed by the user (GitHub #5 and #6).
 *
 * Returns the value with leading and trailing whitespace removed, or null when nothing is
 * left: empty input, or only spaces, tabs or line breaks.
 */
fun validateLabel(raw: String): String? = raw.trim().ifEmpty { null }

/** The outcome of a write that validates user input first. */
sealed interface WriteResult<out T> {
    /** The input was valid and was saved; [value] is what the write produced. */
    data class Saved<out T>(val value: T) : WriteResult<T>

    /** The input was blank after trimming; nothing was written. */
    data object Blank : WriteResult<Nothing>
}
