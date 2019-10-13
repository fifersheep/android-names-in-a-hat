package uk.lobsterdoodle.namepicker.application.di

import dagger.Component
import uk.lobsterdoodle.namepicker.SplashActivity
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.application.AppService
import uk.lobsterdoodle.namepicker.edit.EditGroupDetailsActivity
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity
import uk.lobsterdoodle.namepicker.overview.OverviewActivity
import uk.lobsterdoodle.namepicker.selection.NameSelectionView
import uk.lobsterdoodle.namepicker.selection.SelectionActivity
import uk.lobsterdoodle.namepicker.ui.FlowActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [DependencyInjectionModule::class])
interface DependencyInjectionComponent {

    fun inject(app: App)

    fun inject(service: AppService)

    fun inject(splashActivity: SplashActivity)

    fun inject(flowActivity: FlowActivity)

    fun inject(overviewActivity: OverviewActivity)

    fun inject(editNamesActivity: EditNamesActivity)

    fun inject(editGroupDetailsActivity: EditGroupDetailsActivity)

    fun inject(selectionActivity: SelectionActivity)

    fun inject(nameSelectionView: NameSelectionView)
}
