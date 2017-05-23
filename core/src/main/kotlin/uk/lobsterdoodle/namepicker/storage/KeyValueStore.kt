package uk.lobsterdoodle.namepicker.storage

interface KeyValueStore {
    fun getBool(key: String, defaultValue: Boolean): Boolean
    fun getLong(key: String, defaultValue: Long): Long
    fun getString(key: String, defaultValue: String): String
    operator fun contains(key: String): Boolean
    fun edit(): Edit

    interface Edit {
        fun put(key: String, value: Boolean): Edit
        fun put(key: String, value: Long): Edit
        fun put(key: String, value: String): Edit
        fun remove(key: String): Edit
        fun clear(): Edit
        fun commit()
    }
}
