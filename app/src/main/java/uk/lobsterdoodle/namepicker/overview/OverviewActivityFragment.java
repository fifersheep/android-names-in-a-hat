package uk.lobsterdoodle.namepicker.overview;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.ui.OverviewCard;

/**
 * A placeholder fragment containing a simple view.
 */
public class OverviewActivityFragment extends Fragment {

    @InjectView(R.id.test_overview_card)
    OverviewCard overviewCard;

    public OverviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        overviewCard.bind(new OverviewCardCellData("FanDuel US DFS Android Team", 12));
    }
}
