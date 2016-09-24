package uk.lobsterdoodle.namepicker.application.di;

import javax.inject.Singleton;

import dagger.Component;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.overview.OverviewActivityFragment;

@Singleton
@Component(modules = { DependencyInjectionModule.class })
public interface DependencyInjectionComponent {

    void inject(App app);

    void inject(OverviewActivityFragment overviewActivityFragment);
}
