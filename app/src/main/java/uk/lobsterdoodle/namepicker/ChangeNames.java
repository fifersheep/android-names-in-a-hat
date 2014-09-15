package uk.lobsterdoodle.namepicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.*;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.adapter.NamesListAdapter;
import uk.lobsterdoodle.namepicker.api.ClassroomCoord;
import uk.lobsterdoodle.namepicker.dialog.IInputDialogListener;
import uk.lobsterdoodle.namepicker.dialog.InputDialogFragment;

/** Created by: Scott Laing
 *  Date: 01-Sept-2012 @ 17:28 */

public class ChangeNames extends BaseActivity implements ActionBar.OnNavigationListener, IInputDialogListener {

    private final static int PUPIL_ADD_DIALOG_REQ_CODE = 63;
    public final static int PUPIL_EDIT_DIALOG_REQ_CODE = 64;
    public final static int PUPIL_EXISTS_DIALOG_REQ_CODE = 65;
    private final static int CLASSROOM_ADD_DIALOG_REQ_CODE = 66;
    private final static int CLASSROOM_EDIT_DIALOG_REQ_CODE = 67;
    private final static int CLASS_EXISTS_DIALOG_REQ_CODE = 68;
    private final static int SORT_DIALOG_REQ_CODE = 69;

    Boolean isClassesSpinnerFirstLoad = true;
    Context mExtraContext;
    NamesListAdapter pupilsAdapter;
    ChangeNamesFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_names);

        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        mExtraContext = ChangeNames.this;
        mClassroomCoord = ClassroomCoord.getInstance(this);

        updateClassesSpinner();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // Get the correct name for the current classroom
        if (isClassesSpinnerFirstLoad) {

            // Set classes spinner to current class
            getSupportActionBar().setSelectedNavigationItem(
                    mClassroomCoord.getCurrentClassroomIndex());
        }

        mClassroomCoord.setCurrentClassroomName(itemPosition);

        // Use pupils from the current classroom to populate the pupil name list on screen
        pupilsAdapter = new NamesListAdapter(mExtraContext,
                mClassroomCoord.getCurrentPupils());
        pupilsAdapter.refreshList();
        /** setListAdapter(pupilsAdapter); **/ //TODO: pupilsAdapter.changePupils(mClassroomCoord.getCurrentPupils()

        mFragment = new ChangeNamesFragment();
        mFragment.setAdapter(pupilsAdapter);
        getSupportFragmentManager().beginTransaction().replace(R.id.names_list_fragment, mFragment).commit();

        // Set boolean to show spinner has been loaded before
        isClassesSpinnerFirstLoad = false;
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void showAddPupilDialog() {
        InputDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Add Pupil")
                .setMessage(getString(R.string.dialog_new_pupil_msg))
                .setHint("Pupil name...")
                .setPositiveButtonText("Add")
                .setNegativeButtonText("Cancel")
                .setRequestCode(PUPIL_ADD_DIALOG_REQ_CODE)
                .show();
    }

    private void addPupil(String newName) {
        if (mClassroomCoord.containsPupil(newName)) {
            showPupilExists();
        } else {
            // Add pupil in database
            mClassroomCoord.addPupil(newName);

            // Update adapter
            pupilsAdapter.add(newName);
        }
    }

    protected void showSortPupilsDialog() {
        SimpleDialogFragment
                .createBuilder(this, getSupportFragmentManager())
                .setTitle("Sort Pupils")
                .setMessage(getString(R.string.dialog_sort_pupils_msg))
                .setPositiveButtonText("Yes")
                .setNegativeButtonText("No")
                .setRequestCode(SORT_DIALOG_REQ_CODE)
                .show();
    }

    protected void showAddClassroomDialog() {
        InputDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Add Classroom")
                .setMessage(getString(R.string.dialog_new_class_msg))
                .setHint("Classroom name...")
                .setPositiveButtonText("Add")
                .setNegativeButtonText("Cancel")
                .setRequestCode(CLASSROOM_ADD_DIALOG_REQ_CODE)
                .show();
    }

    private void addClassroom(String classroomName) {
        // Check if classroom name already exists
        if (mClassroomCoord.getClassroom(classroomName) != null) {
            showClassroomExists();
        } else {
            // Add classroom to list
            mClassroomCoord.addClassroom(classroomName);

            // Update the spinner with new list
            updateClassesSpinner();
        }
    }

    private void showClassroomExists() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Class Name Exists")
                .setMessage(getString(R.string.class_name_exists_msg))
                .setRequestCode(CLASS_EXISTS_DIALOG_REQ_CODE)
                .show();
    }

    private void deleteClassroom() {

        final String classroomName = mClassroomCoord.getCurrentClassroomName();

        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_yes_no);
        ((TextView)d.findViewById(R.id.dialog_y_n_title)).setText("Delete Class: " + classroomName);

        TextView message = (TextView)d.findViewById(R.id.dialog_y_n_message);
        message.setText("Are you sure you want to delete '" + classroomName + "' permanently?");

        Button yes = (Button)d.findViewById(R.id.dialog_yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Remove classroom from spinner
                mClassroomCoord.removeClassroom();

                // Update navigation spinner
                updateClassesSpinner();

                // Dismiss dialog
                d.dismiss();
            }
        });
        Button no = (Button)d.findViewById(R.id.dialog_no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        // Shows the dialog
        d.show();
    }

    private void showEditClassroomNameDialog() {

        InputDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Edit Classroom")
                .setMessage(getString(R.string.dialog_edit_class_msg))
                .setHint("Classroom name...")
                .setPositiveButtonText("Add")
                .setNegativeButtonText("Cancel")
                .setRequestCode(CLASSROOM_EDIT_DIALOG_REQ_CODE)
                .show();
    }

    private void editClassroomName(String newName){

        if (mClassroomCoord.containsPupil(newName)) {
            showEditClassroomNameDialog();
        } else {
            // Modify classroom name
            mClassroomCoord.editClassroomName(newName);

            // Update spinner with new classroom names list
            updateClassesSpinner();
        }

    }

    private void showPupilExists() {
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_notification);
        ((TextView)d.findViewById(R.id.dialog_notify_title)).setText("Pupil Name Exists");

        TextView message = (TextView)d.findViewById(R.id.dialog_notify_message);
        message.setText(
                getString(R.string.pupil_name_exists_msg_pt1) +
                getString(R.string.pupil_name_exists_msg_pt2));

        Button okayButton = (Button)d.findViewById(R.id.button_notify_dialog);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPupilDialog();
                d.dismiss();
            }
        });

        // Shows the dialog
        d.show();
    }

    /** Set up menu **/
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.classes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Set options in menu **/
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_name:
                showAddPupilDialog();
                break;
            case R.id.action_add_class:
                showAddClassroomDialog();
                break;
            case R.id.action_edit_class_name:
                showEditClassroomNameDialog();
                break;
            case R.id.action_delete_class:
                deleteClassroom();
                break;
            case R.id.action_sort_names:
                showSortPupilsDialog();
                break;
        }

        return false;
    }

    // Dialog onClickListeners
    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch(requestCode) {
            case SORT_DIALOG_REQ_CODE:
                // Sort pupils in current class
                mClassroomCoord.sortCurrentPupils();

                // Update adapter
                pupilsAdapter.notifyDataSetChanged();
                break;
            case CLASS_EXISTS_DIALOG_REQ_CODE:
                showAddClassroomDialog();
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) { }

    @Override
    public void onNeutralButtonClicked(int requestCode) { }

    @Override
    public void onInputReceived(int requestCode, String input) {
        switch (requestCode) {
            case PUPIL_ADD_DIALOG_REQ_CODE:
                addPupil(input);
                break;
            case PUPIL_EDIT_DIALOG_REQ_CODE:
                break;
            case CLASSROOM_ADD_DIALOG_REQ_CODE:
                addClassroom(input);
                break;
            case CLASSROOM_EDIT_DIALOG_REQ_CODE:
                editClassroomName(input);
                break;
        }
    }
}