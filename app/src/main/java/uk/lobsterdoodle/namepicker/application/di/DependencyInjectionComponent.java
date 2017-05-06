package uk.lobsterdoodle.namepicker.application.di;

import javax.inject.Singleton;

import dagger.Component;
import uk.lobsterdoodle.namepicker.SplashActivity;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.application.AppService;
import uk.lobsterdoodle.namepicker.edit.EditGroupDetailsActivity;
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;
import uk.lobsterdoodle.namepicker.selection.NameSelectionView;
import uk.lobsterdoodle.namepicker.selection.SelectionActivity;
import uk.lobsterdoodle.namepicker.ui.FlowActivity;

@Singleton
@Component(modules = { DependencyInjectionModule.class })
public interface DependencyInjectionComponent {

    void inject(App app);

    void inject(AppService service);

    void inject(SplashActivity splashActivity);

    void inject(FlowActivity flowActivity);

    void inject(OverviewActivity overviewActivity);

    void inject(EditNamesActivity editNamesActivity);

    void inject(EditGroupDetailsActivity editGroupDetailsActivity);

    void inject(SelectionActivity selectionActivity);

    void inject(NameSelectionView nameSelectionView);
}
