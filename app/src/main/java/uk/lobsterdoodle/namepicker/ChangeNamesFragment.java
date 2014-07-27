package uk.lobsterdoodle.namepicker;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

/** Created by: Scott Laing
 *  Date: 01/09/12 @ 17:28 */

public class ChangeNamesFragment extends ListFragment {

    ChangeNames mActivity;
    Context mContext;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = this.getActivity();
        mActivity = ((ChangeNames) mContext);

        //mActivity.setContentView(R.layout.change_names_list);

        // TODO: This list view should be a resource
        ListView listview = getListView();
        listview.setBackgroundColor(Color.WHITE); // Fixes spinner bug somehow...
        int[] colors = {
                0x33B3B5B4, //TODO: Set this color array as resource
                0xBBB3B5B4,
                0xCCB3B5B4,
                0xBBB3B5B4,
                0x33B3B5B4};
        listview.setDivider(new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors));
        listview.setDividerHeight(1);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
    }

    public void setAdapter(NamesListAdapter adapter) {
        setListAdapter(adapter);
    }
}
