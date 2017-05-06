package uk.lobsterdoodle.namepicker.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;
import uk.lobsterdoodle.namepicker.storage.EditGroupDetailsEvent;
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent;
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.storage.GroupNameEditedSuccessfullyEvent;
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent;
import uk.lobsterdoodle.namepicker.ui.FlowActivity;

public class EditGroupDetailsActivity extends FlowActivity {
    private static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @BindView(R.id.create_group_name_input)
    TextInputEditText groupName;

    @BindView(R.id.create_group_done_button)
    Button done;

    @Inject
    EventBus bus;

    private long groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        App.get(this).component().inject(this);
        ButterKnife.bind(this);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);

        done.setOnClickListener(v -> {
            bus.post(groupId == -1 ?
                    new CreateGroupDetailsEvent(groupName.getText().toString()) :
                    new EditGroupDetailsEvent(groupId, groupName.getText().toString()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if (groupId != -1) {
            bus.post(new RetrieveGroupDetailsEvent(groupId));
        }
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void on(GroupDetailsRetrievedSuccessfullyEvent event) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.format("Edit: %s", event.details.name));
        groupName.setText(event.details.name);
    }

    @Subscribe
    public void on(GroupCreationSuccessfulEvent event) {
        TaskStackBuilder.create(this)
                .addNextIntent(OverviewActivity.launchIntent(this))
                .addNextIntent(EditNamesActivity.launchIntent(this, event.getGroupId()))
                .startActivities();
        finish();
    }

    @Subscribe
    public void on(GroupNameEditedSuccessfullyEvent event) {
        TaskStackBuilder.create(this)
                .addNextIntent(OverviewActivity.launchIntent(this))
                .startActivities();
        finish();
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, EditGroupDetailsActivity.class);
    }

    public static Intent launchIntent(Context context, long groupId) {
        final Intent intent = new Intent(context, EditGroupDetailsActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    @Override
    public String getScreenName() {
        return "Edit Group Details";
    }
}
