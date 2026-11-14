package uk.lobsterdoodle.namepicker.shared.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository
import uk.lobsterdoodle.namepicker.shared.selection.DEFAULT_GRID_COLUMNS

/*
 * Key names and value types are 2.7.0's (`name_list` SharedPreferences), so migrated values are
 * read as they were written. Never rename them.
 */

/** The group to reopen on launch, as a Long. Absence means there is none. */
internal const val ACTIVE_GROUP_ID_KEY = "active_group_id"

/** Prefix of each group's sort-order key; the group id follows. The value is a [NameSortType] name. */
internal const val SORT_TYPE_KEY_PREFIX = "group_sort_type_for_group_"

private val activeGroupIdKey = longPreferencesKey(ACTIVE_GROUP_ID_KEY)

private fun sortTypeKey(groupId: Long) = stringPreferencesKey("$SORT_TYPE_KEY_PREFIX$groupId")

/**
 * [PreferencesRepository] on a Preferences DataStore. Each write is a single `edit`, which
 * DataStore serialises, so the read-modify-write calls are atomic. DataStore does all file work
 * in its own scope, never on the caller's thread.
 */
internal class DataStorePreferencesRepository(private val store: DataStore<Preferences>) : PreferencesRepository {

    override val activeGroupId: Flow<Long?> = store.data.map { it[activeGroupIdKey] }.distinctUntilChanged()

    override suspend fun setActiveGroup(id: Long) {
        store.edit { it[activeGroupIdKey] = id }
    }

    override suspend fun clearActiveGroup() {
        store.edit { it.remove(activeGroupIdKey) }
    }

    override suspend fun clearActiveGroupIf(id: Long) {
        store.edit { if (it[activeGroupIdKey] == id) it.remove(activeGroupIdKey) }
    }

    /** Retired with the column toggle (3.0 spec D5): never stored. NAM-10 removes it from the interface. */
    override val gridColumns: Flow<Int> = flowOf(DEFAULT_GRID_COLUMNS)

    /** Retired with the column toggle (3.0 spec D5): does nothing. */
    override suspend fun setGridColumns(count: Int) = Unit

    override fun sortType(groupId: Long): Flow<NameSortType> =
        store.data.map { NameSortType.fromStored(it[sortTypeKey(groupId)]) }.distinctUntilChanged()

    /** [transform] runs inside the store's edit, so it must not call back into this repository. */
    override suspend fun updateSortType(groupId: Long, transform: (NameSortType) -> NameSortType): NameSortType {
        val key = sortTypeKey(groupId)
        lateinit var updated: NameSortType
        store.edit {
            updated = transform(NameSortType.fromStored(it[key]))
            it[key] = updated.name
        }
        return updated
    }

    override suspend fun removeSortType(groupId: Long) {
        store.edit { it.remove(sortTypeKey(groupId)) }
    }
}
