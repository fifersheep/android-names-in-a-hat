package uk.lobsterdoodle.namepicker.application.di;

import dagger.Module;
import dagger.Provides;
import uk.lobsterdoodle.namepicker.events.AndroidEventBus;
import uk.lobsterdoodle.namepicker.events.EventBus;

@Module
public class DependencyInjectionModule {
    private AndroidEventBus bus;

    public DependencyInjectionModule() {
        bus = new AndroidEventBus();
    }

    @Provides
    EventBus providesEventBus() {
        return bus;
    }
}
