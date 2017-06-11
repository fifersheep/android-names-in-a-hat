package uk.lobsterdoodle.namepicker.storage

import android.annotation.SuppressLint
import android.content.SharedPreferences

class SharedPrefsKeyValueStore(private val sharedPreferences: SharedPreferences) : KeyValueStore {

    override fun getBool(key: String, defaultValue: Boolean): Boolean
            = sharedPreferences.getBoolean(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long): Long
            = sharedPreferences.getLong(key, defaultValue)

    override fun getString(key: String, defaultValue: String): String
            = sharedPreferences.getString(key, defaultValue)

    override fun contains(key: String): Boolean
            = sharedPreferences.contains(key)

    @SuppressLint("CommitPrefEdits")
    override fun edit(): KeyValueStore.Edit
            = SharedPrefsEdit(sharedPreferences.edit())

    private inner class SharedPrefsEdit internal constructor(private val edit: SharedPreferences.Editor) : KeyValueStore.Edit {

        override fun put(key: String, value: Boolean): KeyValueStore.Edit {
            edit.putBoolean(key, value)
            return this
        }

        override fun put(key: String, value: Long): KeyValueStore.Edit {
            edit.putLong(key, value)
            return this
        }

        override fun put(key: String, value: String): KeyValueStore.Edit {
            edit.putString(key, value)
            return this
        }

        override fun remove(key: String): KeyValueStore.Edit {
            edit.remove(key)
            return this
        }

        override fun clear(): KeyValueStore.Edit {
            edit.clear()
            return this
        }

        override fun commit() {
            edit.commit()
        }
    }
}
