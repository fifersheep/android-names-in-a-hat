package uk.lobsterdoodle.namepicker.application.di;

import dagger.Module;
import dagger.Provides;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.events.AndroidEventBus;

@Module
public class DependencyInjectionModule {

    @Provides
    EventBus providesEventBus() {
        return new AndroidEventBus();
    }
}
