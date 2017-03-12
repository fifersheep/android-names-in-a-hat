package uk.lobsterdoodle.namepicker.selection;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.events.EventBus;
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent;
import uk.lobsterdoodle.namepicker.ui.FlowActivity;
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent;

public class SelectionActivity extends FlowActivity {
    private static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @InjectView(R.id.selection_list)
    GridView grid;

    @InjectView(R.id.selection_draw_count)
    Spinner drawCount;

    @InjectView(R.id.selection_button_toggle)
    Button toggleButton;

    @InjectView(R.id.selection_button_draw)
    Button drawButton;

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.selection_button_draw)
    public void submit(Button drawButton) {
        bus.post(new DrawNamesFromSelectionEvent((String) drawCount.getSelectedItem(),
            FluentIterable.from(dataWrapper.data())
                    .filter(name -> name.toggledOn)
                    .transform(name -> name.name)
                    .toList()));
    }

    @Inject
    EventBus bus;

    @Inject
    SelectionAdapterDataWrapper dataWrapper;

    private Menu menu;
    private long groupId;
    private List<String> drawCountOptions = new ArrayList<>();
    private SelectionAdapter selectionAdapter;
    private ArrayAdapter<String> drawOptionsAdapter;

    final ImmutableMap<Integer, Runnable> menuItems = ImmutableMap.<Integer, Runnable>builder()
            .put(R.id.menu_action_grid_one, () -> bus.post(new GridColumnSelectedEvent(1)))
            .put(R.id.menu_action_grid_two, () -> bus.post(new GridColumnSelectedEvent(2)))
            .build();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_grid, menu);

        for (Map.Entry<Integer, Runnable> entry : menuItems.entrySet()) {
            addMenuItem(entry.getKey(), entry.getValue());
        }

        bus.post(new LoadSelectionGridPreference());
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe
    public void on(SelectionGridChangedEvent event) {
        grid.setNumColumns(event.gridColumns);

        final MenuItem one = menu.findItem(R.id.menu_action_grid_one);
        one.setVisible(event.nextGridOption == 1);
        one.setEnabled(event.nextGridOption == 1);

        final MenuItem two = menu.findItem(R.id.menu_action_grid_two);
        two.setVisible(event.nextGridOption == 2);
        two.setEnabled(event.nextGridOption == 2);
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

    @Subscribe
    public void on(DisableDrawActionsEvent event) {
        drawCount.setEnabled(false);
        drawButton.setEnabled(false);
    }

    @Subscribe
    public void on(EnableDrawActionsEvent event) {
        drawCount.setEnabled(true);
        drawButton.setEnabled(true);
    }

    private void addMenuItem(int menuItemResId, Runnable runnable) {
        final MenuItem menuItem = menu.findItem(menuItemResId);
        final Drawable wrap = DrawableCompat.wrap(menuItem.getIcon()).mutate();
        DrawableCompat.setTint(wrap, ContextCompat.getColor(this, android.R.color.white));
        menuItem.setIcon(wrap);
        menuItem.setOnMenuItemClickListener((item) -> {
            runnable.run();
            return true;
        });
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
                    (isChecked) -> bus.post(new NameSelectionCheckChangedEvent(position, isChecked)));
            return nameSelectionView;
        }
    }
}
