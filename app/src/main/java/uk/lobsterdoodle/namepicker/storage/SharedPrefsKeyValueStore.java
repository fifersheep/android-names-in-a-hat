package uk.lobsterdoodle.namepicker.storage;

import android.content.SharedPreferences;

public class SharedPrefsKeyValueStore implements KeyValueStore {
    private final SharedPreferences sharedPreferences;

    public SharedPrefsKeyValueStore(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public Edit edit() {
        return new SharedPrefsEdit(sharedPreferences.edit());
    }

    private class SharedPrefsEdit implements Edit {
        private final SharedPreferences.Editor edit;

        SharedPrefsEdit(SharedPreferences.Editor edit) {
            this.edit = edit;
        }

        @Override
        public Edit put(String key, boolean value) {
            edit.putBoolean(key, value);
            return this;
        }

        @Override
        public Edit put(String key, long value) {
            edit.putLong(key, value);
            return this;
        }

        @Override
        public Edit put(String key, String value) {
            edit.putString(key, value);
            return this;
        }

        @Override
        public Edit remove(String key) {
            edit.remove(key);
            return this;
        }

        @Override
        public Edit clear() {
            edit.clear();
            return this;
        }

        @Override
        public void commit() {
            edit.commit();
        }
    }
}
