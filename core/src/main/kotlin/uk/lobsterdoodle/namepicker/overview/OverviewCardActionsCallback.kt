package uk.lobsterdoodle.namepicker.overview

interface OverviewCardActionsCallback {
    fun launchEditGroupNamesScreen(groupId: Long)
    fun launchEditGroupDetailsScreen(groupId: Long)
    fun deleteGroup(groupId: Long)
    fun launchSelectionScreen(groupId: Long)
}
