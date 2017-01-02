package uk.lobsterdoodle.namepicker.storage;

import uk.lobsterdoodle.namepicker.model.GroupDetails;

public class GroupDetailsRetrievedSuccessfullyEvent {
    public final GroupDetails details;

    public GroupDetailsRetrievedSuccessfullyEvent(GroupDetails details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupDetailsRetrievedSuccessfullyEvent)) return false;

        GroupDetailsRetrievedSuccessfullyEvent that = (GroupDetailsRetrievedSuccessfullyEvent) o;

        return details != null ? details.equals(that.details) : that.details == null;

    }

    @Override
    public int hashCode() {
        return details != null ? details.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupDetailsRetrievedSuccessfullyEvent{" +
                "details=" + details +
                '}';
    }
}
