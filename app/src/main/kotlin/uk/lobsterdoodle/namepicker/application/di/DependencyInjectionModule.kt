package uk.lobsterdoodle.namepicker.application.di

import android.app.Application

import dagger.Module
import dagger.Provides
import uk.lobsterdoodle.namepicker.Util
import uk.lobsterdoodle.namepicker.api.ClassroomDbHelper
import uk.lobsterdoodle.namepicker.events.AndroidEventBus
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.selection.NumberGenerator
import uk.lobsterdoodle.namepicker.selection.RandomNumberGenerator
import uk.lobsterdoodle.namepicker.selection.SelectionAdapterDataWrapper
import uk.lobsterdoodle.namepicker.storage.DbHelper
import uk.lobsterdoodle.namepicker.storage.KeyValueStore
import uk.lobsterdoodle.namepicker.storage.SharedPrefsKeyValueStore

@Module
class DependencyInjectionModule(private val app: Application) {
    private val bus: AndroidEventBus = AndroidEventBus()

    @Provides
    internal fun providesEventBus(): EventBus = bus

    @Provides
    internal fun providesSelectionAdapterDataWrapper(bus: EventBus): SelectionAdapterDataWrapper
            = SelectionAdapterDataWrapper(bus)

    @Provides
    internal fun providesKeyValueStore(): KeyValueStore
            = SharedPrefsKeyValueStore(app.getSharedPreferences(Util.FILENAME, 0))

    @Provides
    internal fun providesSQLiteOpenHelper(): DbHelper
            = ClassroomDbHelper(app)

    @Provides
    internal fun providesNumberGenerator(): NumberGenerator
            = RandomNumberGenerator()
}
