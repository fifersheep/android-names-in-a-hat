package uk.lobsterdoodle.namepicker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.application.AppService;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;
import uk.lobsterdoodle.namepicker.selection.SelectionActivity;
import uk.lobsterdoodle.namepicker.storage.GroupActiveEvent;
import uk.lobsterdoodle.namepicker.storage.CheckForActiveGroupEvent;
import uk.lobsterdoodle.namepicker.storage.GroupNotActiveEvent;

public class SplashActivity extends AppCompatActivity implements ServiceConnection {

    @Inject
    EventBus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        App.get(this).component().inject(this);

        Intent intent = new Intent(getApplicationContext(), AppService.class);
        startService(intent);
        bindService(intent, this, Context.BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }

    @Subscribe
    public void on(GroupActiveEvent event) {
        bus.unregister(this);
        startActivity(SelectionActivity.launchIntent(this, event.groupId));
        finish();
    }

    @Subscribe
    public void on(GroupNotActiveEvent event) {
        bus.unregister(this);
        startActivity(OverviewActivity.launchIntent(this));
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        bus.register(this);
        bus.post(new CheckForActiveGroupEvent());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
