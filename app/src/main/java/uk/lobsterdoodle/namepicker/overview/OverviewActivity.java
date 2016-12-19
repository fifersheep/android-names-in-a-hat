package uk.lobsterdoodle.namepicker.overview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupFragment;
import uk.lobsterdoodle.namepicker.edit.EditGroupActivity;

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
        getSupportFragmentManager().beginTransaction()
                .add(R.id.overview_fragment_container, new CreateGroupFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("create_group")
                .commit();
    }

    public void showEditGroupList(long groupId) {
        TaskStackBuilder.create(this)
                .addNextIntent(OverviewActivity.launchIntent(this))
                .addNextIntent(EditGroupActivity.launchIntent(this, groupId))
                .startActivities();
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, OverviewActivity.class);
    }
}