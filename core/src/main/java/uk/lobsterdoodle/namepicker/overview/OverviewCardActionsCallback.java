package uk.lobsterdoodle.namepicker.overview;

public interface OverviewCardActionsCallback {
    void launchEditGroupNamesScreen(long groupId);
    void launchEditGroupDetailsScreen(long groupId);
    void deleteGroup(long groupId);
    void launchSelectionScreen(long groupId);
}
