package uk.lobsterdoodle.namepicker.application;

import android.app.Application;
import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.AndroidLogWrapper;
import uk.lobsterdoodle.namepicker.Log;
import uk.lobsterdoodle.namepicker.analytics.FirebaseAnalytics;
import uk.lobsterdoodle.namepicker.application.di.DaggerDependencyInjectionComponent;
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionComponent;
import uk.lobsterdoodle.namepicker.application.di.DependencyInjectionModule;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.storage.RemoteDb;

public class App extends Application {
    private DependencyInjectionComponent component;

    @Inject EventBus bus;

    @Inject FirebaseAnalytics firebaseAnalytics;

    @Inject RemoteDb remoteDb;

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.INSTANCE.install(new AndroidLogWrapper());
        component = DaggerDependencyInjectionComponent.builder()
                .dependencyInjectionModule(new DependencyInjectionModule(this))
                .build();

        component.inject(this);
        remoteDb.createUser(FirebaseInstanceId.getInstance().getId());
    }

    public DependencyInjectionComponent component() {
        return component;
    }
}
