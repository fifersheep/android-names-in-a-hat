package uk.lobsterdoodle.namepicker.overview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.ui.OverviewCard;

public class OverviewActivityFragment extends Fragment implements OverviewView {

    @InjectView(R.id.overview_group_list)
    RecyclerView groupsRecyclerView;

    @Inject OverviewPresenter presenter;

    private OverviewAdapter overviewAdapter;

    public OverviewActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get(getActivity()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.inject(this, view);
        presenter.onViewCreated(this);
        overviewAdapter = new OverviewAdapter();
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsRecyclerView.setAdapter(overviewAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void dataSetChanged() {
        getActivity().runOnUiThread(() -> overviewAdapter.notifyDataSetChanged());
    }

    private class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OverviewCardViewHolder(new OverviewCard(getContext()));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            OverviewCardViewHolder cardViewHolder = (OverviewCardViewHolder) holder;
            cardViewHolder.bind(presenter.dataFor(position));
        }

        @Override
        public int getItemCount() {
            return presenter.itemCount();
        }
    }

    private class OverviewCardViewHolder extends RecyclerView.ViewHolder {
        public OverviewCard view;

        public OverviewCardViewHolder(OverviewCard view) {
            super(view);
            this.view = view;
        }

        public void bind(OverviewCardCellData data) {
            view.bind(data);
        }
    }
}
