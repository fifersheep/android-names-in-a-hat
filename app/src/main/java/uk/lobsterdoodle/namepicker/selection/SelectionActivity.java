package uk.lobsterdoodle.namepicker.selection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Spinner;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.model.Name;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public class SelectionActivity extends AppCompatActivity {
    private static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @InjectView(R.id.selection_list)
    GridView grid;

    @InjectView(R.id.selection_draw_count)
    Spinner drawCount;

    @Inject
    EventBus bus;

    private long groupId;
    private List<Name> data = new ArrayList<>();
    private SelectionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        adapter = new SelectionAdapter(this);
        grid.setAdapter(adapter);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, transform(asList(1, 2, 3, 4), String::valueOf));
        drawCount.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new RetrieveGroupNamesEvent(groupId));
    }

    @Subscribe
    public void on(GroupNamesRetrievedEvent event) {
        data = event.names;
        adapter.notifyDataSetChanged();
    }

    public static Intent launchIntent(Context context, long groupId) {
        final Intent intent = new Intent(context, SelectionActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private class SelectionAdapter extends BaseAdapter {
        private final Context context;

        SelectionAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final NameSelectionView nameSelectionView = convertView == null
                    ? new NameSelectionView(context)
                    : (NameSelectionView) convertView;
            nameSelectionView.bind(data.get(position));
            return nameSelectionView;
        }
    }
}
