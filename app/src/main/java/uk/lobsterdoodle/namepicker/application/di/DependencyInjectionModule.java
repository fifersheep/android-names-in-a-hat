package uk.lobsterdoodle.namepicker.application.di;

import dagger.Module;
import dagger.Provides;
import uk.lobsterdoodle.namepicker.overview.OverviewPresenter;

@Module
public class DependencyInjectionModule {

    @Provides
    OverviewPresenter providesOverviewPresenter() {
        return new OverviewPresenter();
    }
}
