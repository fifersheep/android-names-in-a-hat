package uk.lobsterdoodle.namepicker.overview;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.addgroup.AddGroupFragment;

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

    public void showAddGroup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.overview_fragment_container, new AddGroupFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("overview_add_group_transaction")
                .commit();
    }
}