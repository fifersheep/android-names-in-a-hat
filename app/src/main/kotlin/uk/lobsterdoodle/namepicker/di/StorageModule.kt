package uk.lobsterdoodle.namepicker.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.lobsterdoodle.namepicker.shared.data.NamesStorage
import uk.lobsterdoodle.namepicker.shared.data.SettingsStorage
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.repository.NameRepository
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor

/**
 * Provides `:shared`, which has no DI annotations. Both storages must be one per process: a second
 * [SettingsStorage] on the same file throws on first use, and [NamesStorage] owns the database.
 * Building either is cheap; the database and the settings file open lazily on first use.
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun namesStorage(@ApplicationContext context: Context): NamesStorage = NamesStorage(context)

    @Provides
    @Singleton
    fun settingsStorage(@ApplicationContext context: Context): SettingsStorage = SettingsStorage(context)

    @Provides
    fun groupRepository(storage: NamesStorage): GroupRepository = storage.groups

    @Provides
    fun nameRepository(storage: NamesStorage): NameRepository = storage.names

    @Provides
    fun preferencesRepository(storage: SettingsStorage): PreferencesRepository = storage.preferences

    @Provides
    fun activeGroup(prefs: PreferencesRepository): ActiveGroup = ActiveGroup(prefs)

    @Provides
    fun groupEditor(groups: GroupRepository, prefs: PreferencesRepository): GroupEditor = GroupEditor(groups, prefs)
}
