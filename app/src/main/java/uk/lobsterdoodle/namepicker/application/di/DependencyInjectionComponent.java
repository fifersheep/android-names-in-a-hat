package uk.lobsterdoodle.namepicker.application.di;

import javax.inject.Singleton;

import dagger.Component;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.application.AppService;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupActivity;
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;

@Singleton
@Component(modules = { DependencyInjectionModule.class })
public interface DependencyInjectionComponent {

    void inject(App app);

    void inject(AppService service);

    void inject(OverviewActivity overviewActivity);

    void inject(EditNamesActivity editNamesActivity);

    void inject(CreateGroupActivity createGroupActivity);
}
