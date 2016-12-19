package uk.lobsterdoodle.namepicker.creategroup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.overview.OverviewActivity;
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent;

public class CreateGroupFragment extends Fragment {

    @Inject EventBus bus;

    @InjectView(R.id.create_group_name_input)
    TextInputEditText groupName;

    @InjectView(R.id.create_group_done_button)
    Button done;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get(getActivity()).component().inject(this);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_group, container, false);
        ButterKnife.inject(this, view);

        done.setOnClickListener(v -> bus.post(new CreateGroupDoneSelectedEvent(groupName.getText().toString())));
        return view;
    }

    @Subscribe
    public void on(GroupCreationSuccessfulEvent event) {
        ((OverviewActivity) getActivity()).showEditGroupList(event.classroomId);
    }
}
