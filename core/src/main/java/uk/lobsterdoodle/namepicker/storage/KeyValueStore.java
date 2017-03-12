package uk.lobsterdoodle.namepicker.storage;

public interface KeyValueStore {
    boolean getBool(String key, boolean defaultValue);

    long getLong(String key, long defaultValue);

    String getString(String key, String defaultValue);

    boolean contains(String key);

    Edit edit();

    interface Edit {
        Edit put(String key, boolean value);

        Edit put(String key, long value);

        Edit put(String key, String value);

        Edit remove(String key);

        Edit clear();

        void commit();
    }
}
