package uk.lobsterdoodle.namepicker.overview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.edit.EditGroupDetailsActivity;
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.selection.SelectionActivity;
import uk.lobsterdoodle.namepicker.storage.ClearActiveGroupEvent;
import uk.lobsterdoodle.namepicker.storage.DeleteGroupEvent;
import uk.lobsterdoodle.namepicker.storage.GroupDeletedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.ui.OverviewCard;

public class OverviewActivity extends AppCompatActivity implements OverviewCardActionsCallback {

    @BindView(R.id.overview_root_layout)
    ViewGroup root;

    @BindView(R.id.overview_group_list)
    RecyclerView groupsRecyclerView;

    @BindView(R.id.overview_add_group)
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
        ButterKnife.bind(this);

        overviewAdapter = new OverviewAdapter();
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(overviewAdapter);
        addGroupButton.setOnClickListener((v) -> startActivity(EditGroupDetailsActivity.launchIntent(this)));

    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new OverviewBecameVisibleEvent());
        bus.post(new ClearActiveGroupEvent());
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

    @Subscribe
    public void on(GroupDeletedSuccessfullyEvent event) {
        bus.post(new OverviewBecameVisibleEvent());
        Snackbar.make(root, String.format("%s deleted", event.groupName), Snackbar.LENGTH_SHORT).show();
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, OverviewActivity.class);
    }

    @Override
    public void launchEditGroupNamesScreen(long groupId) {
        startActivity(EditNamesActivity.launchIntent(this, groupId));
    }

    @Override
    public void launchEditGroupDetailsScreen(long groupId) {
        startActivity(EditGroupDetailsActivity.launchIntent(this, groupId));
    }

    @Override
    public void deleteGroup(long groupId) {
        bus.post(new DeleteGroupEvent(groupId));
    }

    @Override
    public void launchSelectionScreen(long groupId) {
        startActivity(SelectionActivity.launchIntent(this, groupId));
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
            view.bind(OverviewActivity.this, data);
        }
    }
}