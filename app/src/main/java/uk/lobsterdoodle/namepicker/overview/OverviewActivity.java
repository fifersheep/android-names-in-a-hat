package uk.lobsterdoodle.namepicker.overview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupActivity;

public class OverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        showGroupsOverview();
    }

    private void showGroupsOverview() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.overview_fragment_container, new OverviewActivityFragment())
                .commit();
    }

    public void showCreateGroup() {
        startActivity(CreateGroupActivity.launchIntent(this));
    }

    public void showEditGroupList(long groupId) {

    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, OverviewActivity.class);
    }
}