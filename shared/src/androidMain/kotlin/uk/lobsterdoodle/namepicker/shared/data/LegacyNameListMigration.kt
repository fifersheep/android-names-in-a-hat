package uk.lobsterdoodle.namepicker.shared.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** The SharedPreferences file 2.7.0 and earlier kept settings in (`Util.FILENAME` on `master`). */
internal const val LEGACY_PREFERENCES_NAME = "name_list"

/**
 * One-time move of 2.7.0's settings out of `name_list` into the settings DataStore, on first use.
 *
 * Copies `active_group_id` (Long) and every `group_sort_type_for_group_<id>` (String), keeping
 * their names and types. The retired column count and any 1.x leftovers are not copied. Keys the
 * store already has are never overwritten, so a reappearing old file (a restored 2.x backup)
 * cannot undo newer settings.
 *
 * All keys are migrated, so DataStore then clears and deletes `name_list.xml`; the
 * `(context, name)` constructor is what lets it delete the file.
 */
internal fun legacyNameListMigration(context: Context): DataMigration<Preferences> =
    SharedPreferencesMigration(
        context = context,
        sharedPreferencesName = LEGACY_PREFERENCES_NAME,
        migrate = { legacy: SharedPreferencesView, current: Preferences -> current.withLegacySettings(legacy) },
    )

private fun Preferences.withLegacySettings(legacy: SharedPreferencesView): Preferences {
    val present = asMap().keys.mapTo(HashSet()) { it.name }
    val migrated = toMutablePreferences()
    for ((name, value) in legacy.getAll()) {
        if (name in present) continue
        when {
            name == ACTIVE_GROUP_ID_KEY && value is Long -> migrated[longPreferencesKey(name)] = value
            name.startsWith(SORT_TYPE_KEY_PREFIX) && value is String -> migrated[stringPreferencesKey(name)] = value
        }
    }
    // Migrations bypass the factory's freezing wrapper, so hand back an immutable copy.
    return migrated.toPreferences()
}
