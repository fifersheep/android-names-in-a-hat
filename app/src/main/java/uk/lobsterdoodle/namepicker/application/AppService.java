package uk.lobsterdoodle.namepicker.application;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.overview.OverviewUseCase;

public class AppService extends Service {
    private final Binder binder = new LocalBinder();

    @Inject EventBus bus;

    @Inject OverviewUseCase overviewUseCase;

    public AppService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder { }
}
