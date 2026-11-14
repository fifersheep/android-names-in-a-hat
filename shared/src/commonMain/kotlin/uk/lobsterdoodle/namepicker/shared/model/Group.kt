package uk.lobsterdoodle.namepicker.shared.model

/** A named list of names, such as a class. */
data class Group(val id: Long, val label: String)

/** A group as listed on the groups screen, with how many names it holds and how many of those are drawn. */
data class GroupSummary(val group: Group, val nameCount: Int, val drawnCount: Int = 0)
