package uk.lobsterdoodle.namepicker.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.ChangeNames;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.Util;
import uk.lobsterdoodle.namepicker.api.ClassroomCoord;
import uk.lobsterdoodle.namepicker.dialog.IInputDialogListener;
import uk.lobsterdoodle.namepicker.dialog.InputDialogFragment;

/**
 * Created by: Scott Laing
 * Date: 03/10/13 @ 11:41
 **/
public class NamesListAdapter extends ArrayAdapter<String> implements IInputDialogListener {

    private final LayoutInflater mInflater;
    private ClassroomCoord mClassrooms;
    private int mResource;
    private Context mContext;
    private ViewHolder mTempHolder;

    public NamesListAdapter(Context context, ArrayList<String> pupils) {
        super(context, R.layout.names_list_item, pupils);
        mResource = R.layout.names_list_item;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mClassrooms = ClassroomCoord.getInstance(mContext);
    }

    //public void setList(ArrayList<String> list) { mPupils = list; }

    public ArrayList<String> getList() {
        return mClassrooms.getCurrentPupils();
    }

    public void updateDataSource(ClassroomCoord newData) {
        mClassrooms = newData;
        notifyDataSetChanged();
    }

    public void refreshList() {
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if view can be recycled
        View v;
        final ViewHolder holder = new ViewHolder();
        if (convertView == null ||
                ((Integer) convertView.getTag()).intValue() != mResource) {
            v = mInflater.inflate(mResource, parent, false);
            // Set the tag to make sure you can recycle it when you get it as a convert view
            v.setTag(new Integer(mResource));
        } else {
            v = convertView;
        }

        holder.label = (TextView) v.findViewById(R.id.label);
        try{
            holder.label.setText(mClassrooms.getCurrentPupils().get(position));
        } catch (IndexOutOfBoundsException e) {
            position--;
            holder.label.setText(mClassrooms.getCurrentPupils().get(position));
            Log.e("NamePicker",
                    "NameListAdapter > getView: caught an index out of bounds exception");
        }

        // Set the pupil delete button
        holder.delete = (Button) v.findViewById(R.id.button_delete_name);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeletePupilDialog(holder);
            }
        });

        // Set the pupil edit button
        holder.edit = (Button) v.findViewById(R.id.button_edit_name);
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDialog(holder);
            }
        });

        return v;
    }

    private void showEditNameDialog(final ViewHolder holder) {

        ActionBarActivity activity = (ActionBarActivity) mContext;
        String pupil = holder.label.getText().toString();
        mTempHolder = holder;

        InputDialogFragment.createBuilder(mContext, activity.getSupportFragmentManager())
                .setTitle("Edit Pupil")
                .setMessage("Please enter a new name for " + pupil)
                .setHint("New name...")
                .setPositiveButtonText("Save")
                .setNegativeButtonText("Cancel")
                .setRequestCode(ChangeNames.PUPIL_EDIT_DIALOG_REQ_CODE)
                .show();
    }

    public void showDeletePupilDialog(final ViewHolder holder) {

        ActionBarActivity activity = (ActionBarActivity) mContext;
        String pupil = holder.label.getText().toString();
        mTempHolder = holder;

        SimpleDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                .setTitle("Delete \"" + pupil + "\"?")
                .setMessage("Are you sure you want to delete \"" + pupil + "\" permanently?")
                .setPositiveButtonText("Delete")
                .setNegativeButtonText("Cancel")
                .setRequestCode(ChangeNames.PUPIL_DELETE_DIALOG_REQ_CODE)
                .show();

//        final SharedPreferences preferences = mContext.getSharedPreferences(Util.FILENAME, 0);
//        final String currentClassroomName = preferences.getString(
//                mContext.getString(R.string.file_current_classroom, ""),
//                null);
//        final int position = mClassrooms.getCurrentPupils().indexOf(pupil);
//
//        final Dialog d = new Dialog(mContext);
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        d.setContentView(R.layout.dialog_yes_no);
//        ((TextView)d.findViewById(R.id.dialog_y_n_title)).setText("Delete Pupil: " + pupil);
//
//        TextView message = (TextView)d.findViewById(R.id.dialog_y_n_message);
//        message.setText("Are you sure you want to delete '" + pupil + "' permanently?");
//
//        Button yes = (Button)d.findViewById(R.id.dialog_yes);
//        yes.setText("Delete");
//        yes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Remove from adapter list
//                remove(mClassrooms.getCurrentPupilName(position));
//
//                // Remove from database
//                mClassrooms.removePupil(mClassrooms.getCurrentPupils().get(position));
//
//                // Notify adapter of changes
//                //notifyDataSetChanged();
//
//                // Dismiss dialog
//                d.dismiss();
//            }
//        });
//        Button no = (Button)d.findViewById(R.id.dialog_no);
//        no.setText("Cancel");
//        no.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                d.dismiss();
//            }
//        });
//
//        // Shows the dialog
//        d.show();
    }

    public void deletePupil() {
        int position = getPosition(mTempHolder.label.getText().toString());

        // Remove from adapter list
        remove(mClassrooms.getCurrentPupilName(position));

        // Remove from database
        mClassrooms.removePupil(mClassrooms.getCurrentPupils().get(position));

        // Notify adapter of changes
        //notifyDataSetChanged();
    }

    public void addPupils(ArrayList<String> pupils) {
        for (String pupil : pupils) add(pupil);
    }

    public void setTextColor(DialogInterface alert, int color) {
        try {
            Class c = alert.getClass();
            Field mAlert = c.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object alertController = mAlert.get(alert);
            c = alertController.getClass();
            Field mTitleView = c.getDeclaredField("mTitleView");
            mTitleView.setAccessible(true);
            Object dialogTitle = mTitleView.get(alertController);
            TextView dialogTitleView = (TextView)dialogTitle;
            // Set text color on the title
            dialogTitleView.setTextColor(color);
            // To find the horizontal divider, first
            //  get container around the Title
            ViewGroup parent = (ViewGroup)dialogTitleView.getParent();
            // Then get the container around that container
            parent = (ViewGroup)parent.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View v = parent.getChildAt(i);
                if (v instanceof ImageView) {
                    // We got an ImageView, that should be the separator
                    ImageView im = (ImageView)v;
                    // Set a color filter on the image
                    im.setColorFilter(color);
                }
            }
        } catch (Exception e) {
            // Ignore any exceptions, either it works or it doesn't
        }
    }

    public void editPupil(String newName) {

        String originalName = mTempHolder.label.getText().toString();

        if(mClassrooms.getCurrentPupils().contains(newName)){
            ActionBarActivity activity = (ActionBarActivity) mContext;

            SimpleDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                    .setTitle("Class Name Exists")
                    .setMessage(mContext.getString(R.string.class_name_exists_msg))
                    .setRequestCode(ChangeNames.PUPIL_EXISTS_DIALOG_REQ_CODE)
                    .show();

            showDeletePupilDialog(mTempHolder);
        } else {
            mTempHolder.label.setText(newName);
            mClassrooms.changePupilName(originalName, newName);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onPositiveButtonClicked(int i) {    }

    @Override
    public void onNegativeButtonClicked(int i) {    }

    @Override
    public void onNeutralButtonClicked(int i) {    }

    @Override
    public void onInputReceived(int inputId, String input) {    }

    static class ViewHolder
    {
        TextView label;
        Button edit;
        Button delete;
    }
}
