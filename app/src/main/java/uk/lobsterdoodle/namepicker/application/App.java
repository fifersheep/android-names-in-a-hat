package uk.lobsterdoodle.namepicker.application;

import android.app.Application;
import android.content.Context;

import uk.lobsterdoodle.namepicker.AndroidLogWrapper;
import uk.lobsterdoodle.namepicker.Log;
import uk.lobsterdoodle.namepicker.application.di.DaggerDependencyInjectionComponent;
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionComponent;
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionModule;

public class App extends Application {

    private DependencyInjectionComponent component;

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.install(new AndroidLogWrapper());
        component = DaggerDependencyInjectionComponent.builder()
                .dependencyInjectionModule(new DependencyInjectionModule(this))
                .build();

        component.inject(this);
    }

    public DependencyInjectionComponent component() {
        return component;
    }
}
