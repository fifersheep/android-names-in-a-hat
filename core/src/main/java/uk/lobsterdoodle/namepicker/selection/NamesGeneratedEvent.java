package uk.lobsterdoodle.namepicker.selection;

public class NamesGeneratedEvent {
    public final String generatedNames;
    public final boolean multipleNames;

    public NamesGeneratedEvent(String generatedNames, boolean multipleNames) {
        this.generatedNames = generatedNames;
        this.multipleNames = multipleNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamesGeneratedEvent)) return false;

        NamesGeneratedEvent that = (NamesGeneratedEvent) o;

        if (multipleNames != that.multipleNames) return false;
        return generatedNames != null ? generatedNames.equals(that.generatedNames) : that.generatedNames == null;

    }

    @Override
    public int hashCode() {
        int result = generatedNames != null ? generatedNames.hashCode() : 0;
        result = 31 * result + (multipleNames ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NamesGeneratedEvent{" +
                "generatedNames='" + generatedNames + '\'' +
                ", multipleNames=" + multipleNames +
                '}';
    }
}
