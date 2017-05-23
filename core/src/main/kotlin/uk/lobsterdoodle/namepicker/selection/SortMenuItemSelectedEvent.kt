package uk.lobsterdoodle.namepicker.selection

import uk.lobsterdoodle.namepicker.model.Name

data class SortMenuItemSelectedEvent(val groupId: Long, val names: List<Name>)
