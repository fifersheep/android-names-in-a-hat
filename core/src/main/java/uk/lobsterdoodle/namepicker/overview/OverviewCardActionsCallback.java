package uk.lobsterdoodle.namepicker.overview;

public interface OverviewCardActionsCallback {
    void launchEditGroupNamesScreen(long groupId);
    void launchEditGroupDetailsScreen(long groupId);
    void launchDeleteGroupScreen(long groupId);
}
