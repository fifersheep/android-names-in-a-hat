package uk.lobsterdoodle.namepicker.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import uk.lobsterdoodle.namepicker.addgroup.AddNameSelectedEvent;
import uk.lobsterdoodle.namepicker.addgroup.NameCard;
import uk.lobsterdoodle.namepicker.addgroup.NameCardCellData;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.namelist.NameListBecameVisibleEvent;
import uk.lobsterdoodle.namepicker.namelist.ShowNameCardCellData;
import uk.lobsterdoodle.namepicker.storage.GroupSavedSuccessfullyEvent;

import static com.google.common.collect.Lists.transform;

public class EditGroupActivity extends AppCompatActivity {
    public static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @InjectView(R.id.edit_group_name_list)
    RecyclerView nameList;

    @InjectView(R.id.edit_group_button_add_name)
    Button addName;

    @InjectView(R.id.edit_group_done_button)
    Button done;

    @InjectView(R.id.edit_group_input)
    TextInputEditText nameInput;

    @Inject
    EventBus bus;

    private long groupId;
    private NameListAdapter nameListAdapter;
    private List<NameCardCellData> cellData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        nameListAdapter = new NameListAdapter();
        nameList.setLayoutManager(new LinearLayoutManager(this));
        nameList.setAdapter(nameListAdapter);
        addName.setOnClickListener(v -> bus.post(new AddNameSelectedEvent(nameInput.getText().toString())));
        done.setOnClickListener(v -> bus.post(new EditGroupNamesEvent(groupId, transform(cellData, data -> data.name))));
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new NameListBecameVisibleEvent());
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onEvent(ShowNameCardCellData e) {
        cellData = e.cellData;
        runOnUiThread(() -> nameListAdapter.notifyDataSetChanged());
    }

    @Subscribe
    public void on(GroupSavedSuccessfullyEvent event) {
        Snackbar.make(nameList, R.string.group_saved_successfully, Snackbar.LENGTH_SHORT)
                .setCallback(onDismissed(this::finish))
                .show();
    }

    private Snackbar.Callback onDismissed(Runnable runnable) {
        return new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                runnable.run();
            }
        };
    }

    public class NameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NameCardViewHolder(new NameCard(EditGroupActivity.this));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            NameCardViewHolder cardViewHolder = (NameCardViewHolder) holder;
            cardViewHolder.bind(cellData.get(position));
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

        void bind(NameCardCellData data) {
            view.bind(data);
        }
    }

    public static Intent launchIntent(Context context, long groupId) {
        final Intent intent = new Intent(context, EditGroupActivity.class);
        intent.putExtra(EditGroupActivity.EXTRA_GROUP_ID, groupId);
        return intent;
    }
}
