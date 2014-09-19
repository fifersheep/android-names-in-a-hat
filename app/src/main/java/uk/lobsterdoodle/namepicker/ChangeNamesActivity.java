package uk.lobsterdoodle.namepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.adapter.NamesListAdapter;
import uk.lobsterdoodle.namepicker.api.ClassroomCoord;
import uk.lobsterdoodle.namepicker.dialog.IInputDialogListener;
import uk.lobsterdoodle.namepicker.dialog.InputDialogFragment;

/** Created by: Scott Laing
 *  Date: 01-Sept-2012 @ 17:28 */

public class ChangeNamesActivity extends BaseActivity implements ActionBar.OnNavigationListener, IInputDialogListener {

    private final static int PUPIL_ADD_DIALOG_REQ_CODE = 63;
    public final static int PUPIL_EDIT_DIALOG_REQ_CODE = 64;
    public final static int PUPIL_EXISTS_DIALOG_REQ_CODE = 65;
    public final static int PUPIL_DELETE_DIALOG_REQ_CODE = 66;
    private final static int CLASSROOM_ADD_DIALOG_REQ_CODE = 67;
    private final static int CLASSROOM_EDIT_DIALOG_REQ_CODE = 68;
    private static final int CLASSROOM_DELETE_DIALOG_REQ_CODE = 69;
    private final static int CLASS_EXISTS_DIALOG_REQ_CODE = 70;
    private final static int SORT_DIALOG_REQ_CODE = 71;

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

        mExtraContext = ChangeNamesActivity.this;
        mClassroomCoord = ClassroomCoord.getInstance(this);

        updateClassesSpinner();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (isClassesSpinnerFirstLoad) {
            getSupportActionBar().setSelectedNavigationItem(
                    mClassroomCoord.getCurrentClassroomIndex());
        }

        mClassroomCoord.setCurrentClassroomName(itemPosition);

        pupilsAdapter = new NamesListAdapter(mExtraContext,
                mClassroomCoord.getCurrentPupils());
        pupilsAdapter.refreshList();

        mFragment = new ChangeNamesFragment();
        mFragment.setAdapter(pupilsAdapter);
        getSupportFragmentManager().beginTransaction().replace(R.id.names_list_fragment, mFragment).commit();

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
            mClassroomCoord.addPupil(newName);
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
        if (mClassroomCoord.containsClassroom(classroomName)) {
            showClassroomExists();
        } else {
            mClassroomCoord.addClassroom(classroomName);
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

    private void showDeleteClassroomDialog() {
        final String classroomName = mClassroomCoord.getCurrentClassroomName();

        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Delete \"" + classroomName + "\"?")
                .setMessage("Are you sure you want to delete \"" + classroomName +
                        "\"? This will also delete all of the pupils within the classroom.")
                .setPositiveButtonText("Yes")
                .setNegativeButtonText("No")
                .setRequestCode(CLASSROOM_DELETE_DIALOG_REQ_CODE)
                .show();
    }

    private void deleteClassroom() {
        mClassroomCoord.removeClassroom();
        updateClassesSpinner();
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

        if (mClassroomCoord.containsClassroom(newName)) {
            showClassroomExists();
        } else {
            mClassroomCoord.editClassroomName(newName);
            updateClassesSpinner();
        }

    }

    private void showPupilExists() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Pupil Name Exists")
                .setMessage(getString(R.string.pupil_name_exists_msg))
                .setRequestCode(PUPIL_EXISTS_DIALOG_REQ_CODE)
                .show();
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
                showDeleteClassroomDialog();
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
                mClassroomCoord.sortCurrentPupils();
                pupilsAdapter.notifyDataSetChanged();
                break;
            case CLASS_EXISTS_DIALOG_REQ_CODE:
                showEditClassroomNameDialog();
                break;
            case CLASSROOM_DELETE_DIALOG_REQ_CODE:
                deleteClassroom();
                break;
            case PUPIL_DELETE_DIALOG_REQ_CODE:
                pupilsAdapter.deletePupil();
                break;
            case PUPIL_EXISTS_DIALOG_REQ_CODE:
                showAddPupilDialog();
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
                pupilsAdapter.editPupil(input);
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