package uk.lobsterdoodle.namepicker.storage;

public interface KeyValueStore {
    boolean getBool(String key, boolean defaultValue);

    long getLong(String key, long defaultValue);

    String getString(String key, String defaultValue);

    boolean contains(String key);

    KeyValueEdit edit();

    interface KeyValueEdit {
        KeyValueEdit put(String key, boolean value);

        KeyValueEdit put(String key, long value);

        KeyValueEdit put(String key, String value);

        KeyValueEdit remove(String key);

        KeyValueEdit clear();

        void commit();
    }
}
