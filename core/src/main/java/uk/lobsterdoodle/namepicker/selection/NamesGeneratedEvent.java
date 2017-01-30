package uk.lobsterdoodle.namepicker.selection;

import java.util.List;

public class NamesGeneratedEvent {
    public final List<String> generatedNames;

    public NamesGeneratedEvent(List<String> generatedNames) {
        this.generatedNames = generatedNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamesGeneratedEvent)) return false;

        NamesGeneratedEvent that = (NamesGeneratedEvent) o;

        return generatedNames != null ? generatedNames.equals(that.generatedNames) : that.generatedNames == null;
    }

    @Override
    public int hashCode() {
        return generatedNames != null ? generatedNames.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NamesGeneratedEvent{" +
                "generatedNames=" + generatedNames +
                '}';
    }
}
