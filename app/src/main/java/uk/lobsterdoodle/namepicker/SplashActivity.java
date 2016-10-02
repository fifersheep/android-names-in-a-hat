package uk.lobsterdoodle.namepicker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import uk.lobsterdoodle.namepicker.application.AppService;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;

public class SplashActivity extends AppCompatActivity implements ServiceConnection {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(getApplicationContext(), AppService.class);
        startService(intent);
        bindService(intent, this, Context.BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startActivity(new Intent(this, OverviewActivity.class));
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
