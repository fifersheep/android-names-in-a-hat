package uk.lobsterdoodle.namepicker.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;

import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;

public abstract class FlowActivity extends AppCompatActivity {

    @Inject EventBus bus;

    ImmutableMap<Integer, String> orientations = ImmutableMap.of(
            Configuration.ORIENTATION_UNDEFINED, "Undefined",
            Configuration.ORIENTATION_PORTRAIT, "Portrait",
            Configuration.ORIENTATION_LANDSCAPE, "Landscape");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.flow_activity_open_enter, R.anim.flow_activity_open_exit);
        App.get(this).component().inject(this);
        bus.post(new ScreenLaunchedEvent(getScreenName(), orientations.get(getResources().getConfiguration().orientation)));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.flow_activity_close_enter, R.anim.flow_activity_close_exit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract String getScreenName();
}
