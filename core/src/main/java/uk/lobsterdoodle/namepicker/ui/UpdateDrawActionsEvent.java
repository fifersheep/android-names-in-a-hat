package uk.lobsterdoodle.namepicker.ui;

import java.util.List;

import uk.lobsterdoodle.namepicker.selection.SelectionToggleEvent;

public class UpdateDrawActionsEvent {
    public final List<String> drawOptions;
    public final String toggleLabel;
    public final SelectionToggleEvent selectionToggleEvent;

    public UpdateDrawActionsEvent(List<String> drawOptions, String toggleLabel, SelectionToggleEvent selectionToggleEvent) {
        this.drawOptions = drawOptions;
        this.toggleLabel = toggleLabel;
        this.selectionToggleEvent = selectionToggleEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdateDrawActionsEvent)) return false;

        UpdateDrawActionsEvent that = (UpdateDrawActionsEvent) o;

        if (drawOptions != null ? !drawOptions.equals(that.drawOptions) : that.drawOptions != null) return false;
        if (toggleLabel != null ? !toggleLabel.equals(that.toggleLabel) : that.toggleLabel != null) return false;
        return selectionToggleEvent != null ? selectionToggleEvent.equals(that.selectionToggleEvent) : that.selectionToggleEvent == null;

    }

    @Override
    public int hashCode() {
        int result = drawOptions != null ? drawOptions.hashCode() : 0;
        result = 31 * result + (toggleLabel != null ? toggleLabel.hashCode() : 0);
        result = 31 * result + (selectionToggleEvent != null ? selectionToggleEvent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateDrawActionsEvent{" +
                "drawOptions=" + drawOptions +
                ", toggleLabel='" + toggleLabel + '\'' +
                ", selectionToggleEvent=" + selectionToggleEvent +
                '}';
    }
}
