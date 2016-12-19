package uk.lobsterdoodle.namepicker.overview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupActivity;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.ui.OverviewCard;

public class OverviewActivity extends AppCompatActivity {

    @InjectView(R.id.overview_group_list)
    RecyclerView groupsRecyclerView;

    @InjectView(R.id.overview_add_group)
    Button addGroupButton;

    @Inject
    EventBus bus;

    private OverviewAdapter overviewAdapter;
    private List<OverviewCardCellData> cellData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        overviewAdapter = new OverviewAdapter();
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(overviewAdapter);
        addGroupButton.setOnClickListener((v) -> startActivity(CreateGroupActivity.launchIntent(this)));

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
        runOnUiThread(() -> overviewAdapter.notifyDataSetChanged());
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, OverviewActivity.class);
    }

    private class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OverviewCardViewHolder(new OverviewCard(OverviewActivity.this));
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

        OverviewCardViewHolder(OverviewCard view) {
            super(view);
            this.view = view;
        }

        void bind(OverviewCardCellData data) {
            view.bind(data);
        }
    }
}