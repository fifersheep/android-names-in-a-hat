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

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.core.ClassroomCoord;
import uk.lobsterdoodle.namepicker.dialog.InputDialogViewBuilder;
import uk.lobsterdoodle.namepicker.dialog.MyDialogFragment;

/** Created by: Scott Laing
 *  Date: 01-Sept-2012 @ 17:28 */

public class ChangeNames extends BaseActivity implements ActionBar.OnNavigationListener, ISimpleDialogListener {

    private final static int ADD_PUPIL_DIALOG_REQ_CODE = 63;
    private final static int SORT_DIALOG_REQ_CODE = 64;
    private final static int CLASS_EXISTS_DIALOG_REQ_CODE = 65;

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

    /** Shows a dialog with option to save when pressing the back button **/
    @Override
    public void onBackPressed() {
        //Util.saveToFile(this, mClassroomNames, mCurrentClassroom);
        finish();
    }

    /** Adds the user input to 'editableArray' **/
    public void createAddPupilDialog() {

        /*final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_edit_text);
        ((TextView)d.findViewById(R.id.dialog_edit_title)).setText("New Pupil");

        TextView message = (TextView)d.findViewById(R.id.dialog_edit_message);
        message.setText(getString(R.string.dialog_new_pupil_msg));

        final EditText input = (EditText)d.findViewById(R.id.dialog_edit_input);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Pupil Name...");

        Button okayButton = (Button)d.findViewById(R.id.dialog_ok);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newName = input.getText().toString();
                if (mClassroomCoord.containsPupil(newName)) {
                    showPupilExists();
                    d.dismiss();
                } else {
                    // Add pupil in database
                    mClassroomCoord.addPupil(newName);

                    // Update adapter
                    pupilsAdapter.add(newName);
                }
                d.dismiss();
            }
        });
        Button cancelButton = (Button)d.findViewById(R.id.dialog_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        // Brings up keyboard when dialog shows
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    d.getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                }
            }
        });

        // Shows the dialog
        d.show(); */

//        LayoutInflater inflater = getLayoutInflater();
//        inflater.inflate(R.layout.dialog_edit_text, null);
//
//        TextView textView = (TextView) findViewById(R.id.dialog_edit_message);
//
//        EditText editText = (EditText) findViewById(R.id.dialog_edit_input);
//        editText.setHint("Pupil name...");

//        TextInputDialogFragment
//                .createBuilder(this, getSupportFragmentManager())
//                .setHint("Pupil name...")
//                .setTitle("Add Pupil")
//                .setMessage(getString(R.string.dialog_new_pupil_msg))
//                .show();

        final InputDialogViewBuilder viewBuilder = new InputDialogViewBuilder();
        viewBuilder.setMessage(getString(R.string.dialog_new_pupil_msg));
        viewBuilder.setHint("Pupil name...");


        MyDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Add Pupil")
                .setMessage(getString(R.string.dialog_new_pupil_msg))
                .setHint("Pupil name...")
                .setRequestCode(ADD_PUPIL_DIALOG_REQ_CODE)
                .show();


        //////////////////

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        final EditText input = new EditText(this);
//        input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//        input.setHint("Pupil Name...");
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the dialog layout
//        // builder.setView(inflater.inflate(R.layout.dialog_edit_text, null))
//        builder.setView(input)
//                .setTitle("Add Pupil")
//                .setMessage(getString(R.string.dialog_new_pupil_msg))
//                // Add action buttons
//                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        String newName = input.getText().toString();
//                        if (mClassroomCoord.containsPupil(newName)) {
//                            showPupilExists();
//                            //d.dismiss
//                        } else {
//                            // Add pupil in database
//                            mClassroomCoord.addPupil(newName);
//
//                            // Update adapter
//                            pupilsAdapter.add(newName);
//                        }
//
//                        getWindow().setSoftInputMode(
//                                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//                        //d.dismiss();
//                    }
//                });
//
//        // Brings up keyboard when dialog shows
//        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    getWindow()
//                            .setSoftInputMode(
//                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//
//                }
//            }
//        });
//
//        return builder.create();
    }

    protected void sortPupils() {

        SimpleDialogFragment
                .createBuilder(this, getSupportFragmentManager())
                .setTitle("Sort Pupils")
                .setMessage(getString(R.string.dialog_sort_pupils_msg))
                .setPositiveButtonText("Yes")
                .setNegativeButtonText("No")
                .setRequestCode(SORT_DIALOG_REQ_CODE)
                .show();
    }

    protected void createClassroom() {

//        final Dialog d = new Dialog(this);
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        d.setContentView(R.layout.dialog_edit_text);
//        ((TextView)d.findViewById(R.id.dialog_edit_title)).setText("Add Class");
//
//        TextView message = (TextView)d.findViewById(R.id.dialog_edit_message);
//        message.setText(getString(R.string.dialog_new_class_msg));
//
//        final EditText input = (EditText)d.findViewById(R.id.dialog_edit_input);
//        input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//        input.setHint("Class Name...");
//
//        Button okayButton = (Button)d.findViewById(R.id.dialog_ok);
//        okayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Add new classroom to spinner
//                //SharedPreferences preferences = getSharedPreferences(Util.FILENAME, 0);
//                String newClassroomName = input.getText().toString();
//                if (mClassroomCoord.getClassroom(newClassroomName) != null) {
//                    showClassroomExists();
//                } else {
//
//                    mClassroomCoord.addClassroom(newClassroomName);
//
//                    // Add new name to classroom names, update the spinner and save to file
//                    updateClassesSpinner();
//
//                    d.dismiss();
//                }
//            }
//        });
//        Button cancelButton = (Button)d.findViewById(R.id.dialog_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                d.dismiss();
//            }
//        });
//
//        // Brings up keyboard when dialog shows
//        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    d.getWindow()
//                            .setSoftInputMode(
//                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//
//                }
//            }
//        });
//
//        // Shows the dialog
//        d.show();
    }

    private void showClassroomExists() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Class Name Exists");
        builder.setMessage(R.string.class_name_exists_msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int id) {
                createClassroom();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();*/

        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle("Class Name Exists")
                .setMessage(getString(R.string.class_name_exists_msg))
                .setRequestCode(CLASS_EXISTS_DIALOG_REQ_CODE)
                .show();


//        final Dialog d = new Dialog(this);
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        d.setContentView(R.layout.dialog_notification);
//        ((TextView)d.findViewById(R.id.dialog_notify_title)).setText("Class Name Exists");
//
//        TextView message = (TextView)d.findViewById(R.id.dialog_notify_message);
//        message.setText(getString(R.string.class_name_exists_msg));
//
//        Button okayButton = (Button)d.findViewById(R.id.button_notify_dialog);
//        okayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createClassroom();
//                d.dismiss();
//            }
//        });
//
//        // Shows the dialog
//        d.show();
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

    private void editClassroomName() {

//        final Dialog d = new Dialog(this);
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        d.setContentView(R.layout.dialog_edit_text);
//        ((TextView)d.findViewById(R.id.dialog_edit_title)).setText("Edit Class Name");
//
//        TextView message = (TextView)d.findViewById(R.id.dialog_edit_message);
//        message.setText(getString(R.string.dialog_edit_class_msg));
//
//        final EditText input = (EditText)d.findViewById(R.id.dialog_edit_input);
//        input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//        input.setText(mClassroomCoord.getCurrentClassroomName());
//
//        Button okayButton = (Button)d.findViewById(R.id.dialog_ok);
//        okayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editClassroomName(input.getText().toString());
//                d.dismiss();
//            }
//        });
//        Button cancelButton = (Button)d.findViewById(R.id.dialog_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                d.dismiss();
//            }
//        });
//
//        // Brings up keyboard when dialog shows
//        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    d.getWindow()
//                            .setSoftInputMode(
//                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//
//                }
//            }
//        });
//
//        // Shows the dialog
//        d.show();
    }

    private void editClassroomName(String newName){

        // Modify classroom name
        mClassroomCoord.editClassroomName(newName);

        // Update spinner with new classroom names list
        updateClassesSpinner();
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
                createAddPupilDialog();
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
                createAddPupilDialog();
                break;
            case R.id.action_add_class:
                createClassroom();
                break;
            case R.id.action_edit_class_name:
                editClassroomName();
                break;
            case R.id.action_delete_class:
                deleteClassroom();
                break;
            case R.id.action_sort_names:
                sortPupils();
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
                createClassroom();
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }
}