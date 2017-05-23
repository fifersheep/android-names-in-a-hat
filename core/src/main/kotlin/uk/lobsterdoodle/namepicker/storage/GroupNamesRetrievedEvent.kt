package uk.lobsterdoodle.namepicker.storage

import uk.lobsterdoodle.namepicker.model.Name

data class GroupNamesRetrievedEvent(val groupId: Long, val names: List<Name>)
