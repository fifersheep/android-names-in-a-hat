package uk.lobsterdoodle.namepicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.api.ClassroomCoord;
import com.newrelic.agent.android.NewRelic;


public class DrawActivity extends BaseActivity implements View.OnClickListener, ActionBar.OnNavigationListener {

    ArrayList<CheckBox> mCheckBoxArray = new ArrayList<CheckBox>();
    Boolean isClassesSpinnerFirstLoad = true;
    Button mButtonClear;
    Button mButtonDraw;
    LinearLayout mColumnLeft;
    LinearLayout mColumnRight;
    Spinner mSpinnerDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        NewRelic.withApplicationToken(
                "AAac33f7702cd4bb3effd74df8e90cc829bd4d5666").start(this.getApplication());

        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        bar.setIcon(R.drawable.ic_logo);

        // Initialise layout
        mColumnLeft = (LinearLayout) findViewById(R.id.llLeft);
        mColumnRight = (LinearLayout) findViewById(R.id.llRight);
        mButtonClear = (Button) findViewById(R.id.button_clear);
        mButtonDraw = (Button) findViewById(R.id.button_go);
        mSpinnerDraw = (Spinner)findViewById(R.id.action_spinner);
        mButtonClear.setOnClickListener(this);
        mButtonDraw.setOnClickListener(this);

        mClassroomCoord = ClassroomCoord.getInstance(this);

        String[] oldNames = getOldNameList();
        if (oldNames != null) {
            mClassroomCoord.addClassroom("Original Names", oldNames);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateClassesSpinner();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_go:
                startDraw();
                break;
            case R.id.button_clear:
                toggleChecks();
                break;
            case R.id.button_draw_dialog_ok:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        // If activity is loading for the first time
        if (isClassesSpinnerFirstLoad) {

            // Set classes spinner to current class
            getSupportActionBar().setSelectedNavigationItem(
                    mClassroomCoord.getCurrentClassroomIndex());
        }

        // Set "current classroom" name
        mClassroomCoord.setCurrentClassroomName(itemPosition);

        // Load pupils into checkboxes
        updateChecks();

        // Set boolean to show spinner has been loaded before
        isClassesSpinnerFirstLoad = false;
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }

    @Deprecated
    protected String[] getOldNameList() {
        String FILENAME = "name_list";
        SharedPreferences sharedPreferences = getSharedPreferences(FILENAME, 0);
        String loadedNames = sharedPreferences.getString("names", null);
        if (loadedNames == null) {
            Log.d("::: LOADED NAMES :::", "returning null");
            return null;
        } else {
            sharedPreferences.edit().clear().commit();
            Log.d("::: LOADED NAMES :::", loadedNames);
            return loadedNames.split(", ");
        }
    }

    protected void updateChecks() {

        mColumnLeft.removeAllViews(); //TODO: List wasn't updating, but is now. KEpp an eye out.
        mColumnRight.removeAllViews();
        mCheckBoxArray.clear();

        // Loop to create CheckBoxes
        for (int i = 0; i < mClassroomCoord.getCurrentClassroomSize(); i++) {

            CheckBox cb = new CheckBox(this);
            cb.setPadding(0, 25, 0, 25);
            cb.setId(i);
            //cb.setButtonDrawable(R.drawable.checkbox);
            cb.setTextColor(getResources().getColor(R.color.grey_dark));
            cb.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            cb.setText(mClassroomCoord.getCurrentPupilName(i));
            mCheckBoxArray.add(cb);

            // If iterator is odd, add to left column. Otherwise add to right column
            if ((i & 1) == 0) {
                mColumnLeft.addView(cb);
            } else {
                mColumnRight.addView(cb);
            }
        }
        setCheckBoxListeners();
    }

    private void updateDrawSpinner(int numberOfCheckedNames) {

        String[] maxNumberSelectable = new String[numberOfCheckedNames];

        for (int i = 0; i < numberOfCheckedNames; i++) {
            maxNumberSelectable[i] = Integer.toString(i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.draw_spinner_item, maxNumberSelectable);
        adapter.setDropDownViewResource(R.layout.draw_spinner_dropdown_item);
        mSpinnerDraw.setAdapter(adapter);
    }

    protected void setCheckBoxListeners() {

        for (CheckBox cb : mCheckBoxArray) {
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                int counter = 0;
                for (CheckBox cb : mCheckBoxArray) {
                    if (cb.isChecked()) {
                        counter++;
                    }
                }
                updateDrawSpinner(counter);
                }
            });
        }
    }

    private void toggleChecks() {
        boolean noBoxesAreChecked = true;
        for (CheckBox cb : mCheckBoxArray) {
            if (cb.isChecked()) {
                noBoxesAreChecked = false;
            }
            cb.setChecked(false);
        }
        updateDrawSpinner(0);

        if (noBoxesAreChecked) {
            for (CheckBox cb : mCheckBoxArray) {
                cb.setChecked(true);
            }
            updateDrawSpinner(mCheckBoxArray.size());
        }
    }

    protected void startDraw() {
        int numberToDraw = 0;

        try {
            numberToDraw = Integer.parseInt((String) mSpinnerDraw.getSelectedItem());
        } catch (NumberFormatException e) {
            Log.e(
                    getString(R.string.app_tag),
                    "NumberFormatException: DrawActivity > startDraw()", e);

            Toast.makeText(this,
                    "NumberFormatException: DrawActivity > startDraw()",
                    Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            Log.e(
                    getString(R.string.app_tag),
                    "NullPointerException: DrawActivity > startDraw()", e);
            Toast.makeText(this,
                    "NullPointerException: DrawActivity > startDraw()" + e,
                    Toast.LENGTH_LONG).show();
        } finally {

            String drawn = Hat.getInstance()
                    .setNameList(mCheckBoxArray).draw(numberToDraw);

            SimpleDialogFragment
                    .createBuilder(this, getSupportFragmentManager())
                    .setTitle("Drawn Names...")
                    .setMessage(drawn)
                    .show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        updateClassesSpinner();

        int itemPosition = mClassroomCoord.getCurrentClassroomIndex();
        getSupportActionBar().setSelectedNavigationItem(itemPosition);

        updateChecks();
        updateDrawSpinner(0);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.draw_action_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_edit_class);
        MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_edit_class:
                item.setEnabled(false);
                Intent i = new Intent(this, ChangeNamesActivity.class);
                startActivityForResult(i, 1);
                break;
        }
        return true;
    }
}
