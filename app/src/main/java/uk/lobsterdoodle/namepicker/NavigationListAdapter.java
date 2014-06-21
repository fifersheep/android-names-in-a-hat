package uk.lobsterdoodle.namepicker;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by: Scott Laing
 * Date: 12/09/13 @ 11:18
 */
public class NavigationListAdapter extends ArrayAdapter<String>{

    private final LayoutInflater mInflater;
    private String[] mObjects;

    public NavigationListAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Return a view which appears in the action bar.
        // Check if view can be recycled
        View v;
        if (convertView == null || ((Integer) convertView.getTag()).intValue()
                != R.layout.navigation_list_item) {
            v = mInflater.inflate(R.layout.navigation_list_item, parent, false);
            // Set the tag to make sure you can recycle it when you get it as a convert view
            v.setTag(new Integer(R.layout.navigation_list_item));
        } else {
            v = convertView;
        }

        TextView title = (TextView) v.findViewById(R.id.nav_title);
        title.setText("Selected Class:");
        TextView subtitle = (TextView) v.findViewById(R.id.nav_subtitle);
        subtitle.setText(mObjects[position]);
        //subtitle.setTextSize(16f);

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        // Ensure that convertView in not null
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_spinner_dropdown_item,
                    parent, false);
        }

        // Set the value of each list item
        ((TextView) convertView.findViewById(android.R.id.text1))
                .setText(mObjects[position]);

        return convertView;
    }
}
