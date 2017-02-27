package uk.lobsterdoodle.namepicker.application.di;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import uk.lobsterdoodle.namepicker.Util;
import uk.lobsterdoodle.namepicker.api.ClassroomDbHelper;
import uk.lobsterdoodle.namepicker.events.AndroidEventBus;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.selection.NumberGenerator;
import uk.lobsterdoodle.namepicker.selection.RandomNumberGenerator;
import uk.lobsterdoodle.namepicker.selection.SelectionAdapterDataWrapper;
import uk.lobsterdoodle.namepicker.storage.DbHelper;
import uk.lobsterdoodle.namepicker.storage.KeyValueStore;
import uk.lobsterdoodle.namepicker.storage.SharedPrefsKeyValueStore;

@Module
public class DependencyInjectionModule {
    private final Application app;
    private AndroidEventBus bus;

    public DependencyInjectionModule(Application app) {
        this.app = app;
        bus = new AndroidEventBus();
    }

    @Provides
    EventBus providesEventBus() {
        return bus;
    }

    @Provides
    SelectionAdapterDataWrapper providesSelectionAdapterDataWrapper(EventBus bus) {
        return new SelectionAdapterDataWrapper(bus);
    }

    @Provides
    KeyValueStore providesKeyValueStore() {
        return new SharedPrefsKeyValueStore(app.getSharedPreferences(Util.FILENAME, 0));
    }

    @Provides
    DbHelper providesSQLiteOpenHelper() {
        return ClassroomDbHelper.getInstance(app);
    }

    @Provides
    NumberGenerator providesNumberGenerator() {
        return new RandomNumberGenerator();
    }
}
