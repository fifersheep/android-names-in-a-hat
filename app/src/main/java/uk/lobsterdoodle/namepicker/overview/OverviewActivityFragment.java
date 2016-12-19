package uk.lobsterdoodle.namepicker.overview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.ui.OverviewCard;

public class OverviewActivityFragment extends Fragment {

    @InjectView(R.id.overview_group_list)
    RecyclerView groupsRecyclerView;

    @InjectView(R.id.overview_add_group)
    Button addGroupButton;

    @Inject EventBus bus;

    private OverviewAdapter overviewAdapter;
    private List<OverviewCardCellData> cellData = new ArrayList<>();

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
        overviewAdapter = new OverviewAdapter();
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsRecyclerView.setAdapter(overviewAdapter);
        addGroupButton.setOnClickListener((v) -> ((OverviewActivity) getActivity()).showCreateGroup());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new OverviewBecameVisibleEvent());
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onEvent(OverviewRetrievedEvent retrievedEvent) {
        cellData = retrievedEvent.cellData;
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
            cardViewHolder.bind(cellData.get(position));
        }

        @Override
        public int getItemCount() {
            return cellData.size();
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
