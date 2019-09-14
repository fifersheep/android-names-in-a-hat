package uk.lobsterdoodle.namepicker.application

import android.app.Application
import android.content.Context
import uk.lobsterdoodle.namepicker.AndroidLogWrapper
import uk.lobsterdoodle.namepicker.Log
import uk.lobsterdoodle.namepicker.application.di.DaggerDependencyInjectionComponent
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionComponent
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionModule
import uk.lobsterdoodle.namepicker.events.EventBus
import javax.inject.Inject

class App : Application() {
    private lateinit var component: DependencyInjectionComponent

    @Inject lateinit var bus: EventBus

    override fun onCreate() {
        super.onCreate()
        Log.install(AndroidLogWrapper())
        component = DaggerDependencyInjectionComponent.builder()
                .dependencyInjectionModule(DependencyInjectionModule(this))
                .build()

        component.inject(this)
    }

    fun component() = component

    companion object {
        operator fun get(context: Context) = context.applicationContext as App
    }
}
