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
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.google.common.collect.FluentIterable;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent;

public class SelectionActivity extends AppCompatActivity {
    private static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @InjectView(R.id.selection_list)
    GridView grid;

    @InjectView(R.id.selection_draw_count)
    Spinner drawCount;

    @InjectView(R.id.selection_button_toggle)
    Button toggleButton;

    @OnClick(R.id.selection_button_draw)
    public void submit(Button drawButton) {
        drawButton.setOnClickListener(v -> bus.post(new DrawNamesFromSelectionEvent((String) drawCount.getSelectedItem(),
                FluentIterable.from(dataWrapper.data())
                        .filter(name -> name.toggledOn)
                        .transform(name -> name.name)
                        .toList())));
    }

    @Inject
    EventBus bus;

    @Inject
    SelectionAdapterDataWrapper dataWrapper;

    private long groupId;
    private List<String> drawCountOptions = new ArrayList<>();
    private SelectionAdapter selectionAdapter;
    private ArrayAdapter<String> drawOptionsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        selectionAdapter = new SelectionAdapter(this);
        grid.setAdapter(selectionAdapter);

        drawOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, drawCountOptions);
        drawCount.setAdapter(drawOptionsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        bus.post(new RetrieveGroupNamesEvent(groupId));
    }

    @Subscribe
    public void on(UpdateDrawActionsEvent event) {
        toggleButton.setText(event.toggleLabel);
        toggleButton.setOnClickListener(v -> bus.post(event.selectionToggleEvent));

        drawOptionsAdapter.clear();
        drawOptionsAdapter.addAll(event.drawOptions);
    }

    @Subscribe
    public void on(NamesGeneratedEvent event) {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(getString(event.multipleNames
                        ? R.string.generated_names_dialog_title_multiple
                        : R.string.generated_names_dialog_title_singular))
                .setMessage(event.generatedNames)
                .setPositiveButtonText(getString(R.string.generated_names_dialog_positive_button))
                .show();
    }

    @Subscribe
    public void on(SelectionDataUpdatedEvent event) {
        selectionAdapter.notifyDataSetChanged();
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
            return dataWrapper.count();
        }

        @Override
        public Object getItem(int position) {
            return dataWrapper.item(position);
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

            nameSelectionView.bind(dataWrapper.item(position),
                    (buttonView, isChecked) -> bus.post(new NameSelectionCheckChangedEvent(position, isChecked)));
            return nameSelectionView;
        }
    }
}
