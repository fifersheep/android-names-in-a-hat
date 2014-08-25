package uk.lobsterdoodle.namepicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

import uk.lobsterdoodle.namepicker.adapter.NavigationListAdapter;
import uk.lobsterdoodle.namepicker.api.ClassroomCoord;

/** Created by: Scott Laing
 *  Date: 15/05/14 @ 23:42 */

abstract class BaseActivity extends ActionBarActivity implements ActionBar.OnNavigationListener{

    ClassroomCoord mClassroomCoord;
    SharedPreferences mPreferences;
    NavigationListAdapter mNavListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(Util.FILENAME, 0);
    }

    protected void updateClassesSpinner() {

        // Get classroom names
        ArrayList<String> classroomNames = mClassroomCoord.getClassroomNames();

        // Use an ArrayAdapter to populate the spinner with the array of names
        mNavListAdapter = new NavigationListAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,
                classroomNames.toArray(new String[classroomNames.size()]));
        mNavListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getSupportActionBar().setListNavigationCallbacks(mNavListAdapter, this);

        // Set classes spinner to current class
        int position = mClassroomCoord.getCurrentClassroomIndex();
        getSupportActionBar().setSelectedNavigationItem(position);

    }

    @Override
    abstract public boolean onNavigationItemSelected(int i, long l);
}
