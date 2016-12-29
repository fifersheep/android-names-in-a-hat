package uk.lobsterdoodle.namepicker.creategroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent;

public class CreateGroupActivity extends AppCompatActivity {

    @InjectView(R.id.create_group_name_input)
    TextInputEditText groupName;

    @InjectView(R.id.create_group_done_button)
    Button done;

    @Inject
    EventBus bus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        done.setOnClickListener(v -> bus.post(new CreateGroupDoneSelectedEvent(groupName.getText().toString())));
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void on(GroupCreationSuccessfulEvent event) {
        TaskStackBuilder.create(this)
                .addNextIntent(OverviewActivity.launchIntent(this))
                .addNextIntent(EditNamesActivity.launchIntent(this, event.groupId))
                .startActivities();
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, CreateGroupActivity.class);
    }
}
