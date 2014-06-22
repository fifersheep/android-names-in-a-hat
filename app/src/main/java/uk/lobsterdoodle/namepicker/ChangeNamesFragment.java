package uk.lobsterdoodle.namepicker;


import android.content.Context;
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

        ListView listview = getListView();
        listview.setBackgroundResource(R.color.grey_dark); // Fixes spinner bug somehow...
        int[] colors = {0x33B3B5B4, 0xDDB3B5B4, 0xEEB3B5B4, 0xDDB3B5B4, 0x33B3B5B4}; //TODO: Set this as resource
        listview.setDivider(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors));
        listview.setDividerHeight(1);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);

        if(mContext instanceof ChangeNames) {
            String str = "Fake";
            //((ChangeNames) mContext).onListItemClick(l, v, position, id);
        }
    }

    public void setAdapter(NamesListAdapter adapter) {
        setListAdapter(adapter);
    }
}
