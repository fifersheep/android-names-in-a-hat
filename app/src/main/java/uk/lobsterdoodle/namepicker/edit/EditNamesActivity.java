package uk.lobsterdoodle.namepicker.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent;
import uk.lobsterdoodle.namepicker.addgroup.NameCard;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.storage.DeleteNameEvent;
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;
import uk.lobsterdoodle.namepicker.storage.NameAddedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.storage.NameDeletedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent;
import uk.lobsterdoodle.namepicker.ui.FlowActivity;

public class EditNamesActivity extends FlowActivity implements NameCardActions {
    public static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @BindView(R.id.edit_names_root_layout)
    ViewGroup root;

    @BindView(R.id.edit_group_names_list)
    RecyclerView nameList;

    @BindView(R.id.edit_group_names_button_add_name)
    Button addName;

    @BindView(R.id.edit_group_names_done_button)
    Button done;

    @BindView(R.id.edit_group_names_input)
    TextInputEditText nameInput;

    @Inject
    EventBus bus;

    private long groupId;
    private NameListAdapter nameListAdapter;
    private List<Name> cellData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_names);
        App.get(this).component().inject(this);
        ButterKnife.bind(this);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        nameListAdapter = new NameListAdapter();
        nameList.setLayoutManager(new LinearLayoutManager(this));
        nameList.setAdapter(nameListAdapter);
        addName.setOnClickListener(v -> bus.post(new AddNameToGroupEvent(groupId, nameInput.getText().toString())));
        done.setOnClickListener(v -> finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new RetrieveGroupNamesEvent(groupId));
        bus.post(new RetrieveGroupDetailsEvent(groupId));
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void on(GroupDetailsRetrievedSuccessfullyEvent event) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.format("Names: %s", event.getDetails().getName()));
    }

    @Subscribe
    public void on(GroupNamesRetrievedEvent event) {
        cellData = event.getNames();
        runOnUiThread(() -> nameListAdapter.notifyDataSetChanged());
    }

    @Subscribe
    public void on(NameAddedSuccessfullyEvent event) {
        nameInput.setText("");
    }

    @Subscribe
    public void on(NameDeletedSuccessfullyEvent event) {
        bus.post(new RetrieveGroupNamesEvent(groupId));
        Snackbar.make(root, String.format("%s deleted", event.getName()), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void deleteName(long nameId) {
        bus.post(new DeleteNameEvent(groupId, nameId));
    }

    public class NameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NameCardViewHolder(new NameCard(EditNamesActivity.this));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            NameCardViewHolder cardViewHolder = (NameCardViewHolder) holder;
            cardViewHolder.bind(EditNamesActivity.this, cellData.get(position));
        }

        @Override
        public int getItemCount() {
            return cellData.size();
        }
    }

    public class NameCardViewHolder extends RecyclerView.ViewHolder {
        public NameCard view;

        NameCardViewHolder(NameCard view) {
            super(view);
            this.view = view;
        }

        void bind(NameCardActions nameCardActions, Name data) {
            view.bind(nameCardActions, data);
        }
    }

    public static Intent launchIntent(Context context, long groupId) {
        final Intent intent = new Intent(context, EditNamesActivity.class);
        intent.putExtra(EditNamesActivity.EXTRA_GROUP_ID, groupId);
        return intent;
    }

    @Override
    public String getScreenName() {
        return "Edit Names Screen";
    }
}
