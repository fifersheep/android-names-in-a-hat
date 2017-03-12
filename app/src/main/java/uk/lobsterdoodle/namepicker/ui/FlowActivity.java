package uk.lobsterdoodle.namepicker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import uk.lobsterdoodle.namepicker.R;

public class FlowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.flow_activity_open_enter, R.anim.flow_activity_open_exit);
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
}
