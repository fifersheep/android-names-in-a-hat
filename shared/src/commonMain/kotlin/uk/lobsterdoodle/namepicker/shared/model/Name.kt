package uk.lobsterdoodle.namepicker.shared.model

/**
 * One name in a group.
 *
 * [selected] decides whether the name takes part in a draw. [drawn] records that the name
 * has already been drawn; the two flags are independent.
 */
data class Name(
    val id: Long,
    val groupId: Long,
    val text: String,
    val selected: Boolean,
    val drawn: Boolean = false,
)
