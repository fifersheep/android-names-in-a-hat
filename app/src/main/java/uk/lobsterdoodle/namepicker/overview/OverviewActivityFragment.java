package uk.lobsterdoodle.namepicker.overview;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.lobsterdoodle.namepicker.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class OverviewActivityFragment extends Fragment {

    public OverviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }
}
