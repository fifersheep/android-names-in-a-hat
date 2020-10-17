package uk.lobsterdoodle.namepicker.selection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent
import uk.lobsterdoodle.namepicker.overview.OverviewActivity
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent
import uk.lobsterdoodle.namepicker.storage.SetActiveGroupEvent
import uk.lobsterdoodle.namepicker.ui.FlowActivity
import uk.lobsterdoodle.namepicker.ui.UpdateDrawActionsEvent
import java.util.*
import javax.inject.Inject

class SelectionActivity : FlowActivity() {

    @BindView(R.id.selection_list) lateinit var grid: GridView

    @BindView(R.id.selection_empty_text_view) lateinit var emptyView: View

    @BindView(R.id.selection_draw_count_selector) lateinit var drawCountSelector: Spinner

    @BindView(R.id.selection_button_toggle) lateinit var toggleButton: Button

    @BindView(R.id.selection_button_draw) lateinit var drawButton: Button

    @BindView(R.id.selection_draw_counter) lateinit var drawCounter: TextView

    @Suppress("UNUSED_PARAMETER")
    @OnClick(R.id.selection_button_draw)
    fun submit(drawButton: Button) {
        bus.post(DrawNamesFromSelectionEvent(drawCountSelector.selectedItem as String,
            dataWrapper.data()
                    .filter { it.toggledOn }
                    .map { it.name }
                    .toList()))
    }

    @Inject lateinit var bus: EventBus

    @Inject lateinit var dataWrapper: SelectionAdapterDataWrapper

    private lateinit var menu: Menu
    private lateinit var selectionAdapter: SelectionAdapter
    private lateinit var drawOptionsAdapter: ArrayAdapter<String>

    private var groupId: Long = 0
    private val drawCountOptions = ArrayList<String>()

    internal val menuItems = mapOf(
            R.id.menu_action_grid_one to Runnable { bus.post(GridColumnSelectedEvent(1)) },
            R.id.menu_action_grid_two to Runnable { bus.post(GridColumnSelectedEvent(2)) },
            R.id.menu_action_sort to Runnable { bus.post(SortMenuItemSelectedEvent(groupId, dataWrapper.data())) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)
        App.get(this).component().inject(this)
        ButterKnife.bind(this)

        groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1)
        dataWrapper.forGroup(groupId)
        selectionAdapter = SelectionAdapter(this)
        grid.adapter = selectionAdapter

        drawOptionsAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, drawCountOptions)
        drawCountSelector.adapter = drawOptionsAdapter
    }

    override fun onResume() {
        super.onResume()
        dataWrapper.resume()
        bus.register(this)
        bus.post(RetrieveGroupNamesEvent(groupId))
        bus.post(RetrieveGroupDetailsEvent(groupId))
    }

    override fun onPause() {
        dataWrapper.pause()
        bus.unregister(this)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_selection, menu)
        menuItems.forEach { addMenuItem(it.key, it.value) }

        bus.post(LoadSelectionGridPreference)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) backToOverview()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        backToOverview()
        super.onBackPressed()
    }

    private fun backToOverview() {
        TaskStackBuilder.create(this)
                .addNextIntent(OverviewActivity.launchIntent(this))
                .startActivities()
        finish()
    }

    @Subscribe
    fun on(event: GroupDetailsRetrievedSuccessfullyEvent) {
        supportActionBar?.title = event.details.name
        bus.post(SetActiveGroupEvent(groupId))
    }

    @Subscribe
    fun on(event: SelectionGridChangedEvent) {
        grid.numColumns = event.gridColumns

        val one = menu.findItem(R.id.menu_action_grid_one)
        one.isVisible = event.nextGridOption == 1
        one.isEnabled = event.nextGridOption == 1

        val two = menu.findItem(R.id.menu_action_grid_two)
        two.isVisible = event.nextGridOption == 2
        two.isEnabled = event.nextGridOption == 2
    }

    @Subscribe
    fun on(event: UpdateDrawActionsEvent) {
        toggleButton.text = event.toggleLabel
        toggleButton.setOnClickListener { bus.post(event.selectionToggleEvent) }

        drawOptionsAdapter.clear()
        drawOptionsAdapter.addAll(event.drawOptions)

        drawCounter.text = event.drawCounterText
    }

    @Subscribe
    fun on(event: NamesGeneratedEvent) {
        val title = if (event.multipleNames)
            R.string.generated_names_dialog_title_multiple
            else R.string.generated_names_dialog_title_singular

        SimpleDialogFragment.createBuilder(this, supportFragmentManager)
                .setTitle(getString(title))
                .setMessage(event.generatedNames)
                .setPositiveButtonText(getString(R.string.generated_names_dialog_positive_button))
                .show()
    }

    @Subscribe
    fun on(event: SelectionDataUpdatedEvent) {
        selectionAdapter.notifyDataSetChanged()
    }

    @Subscribe
    fun on(event: EnableSelectionEmptyStateEvent) {
        emptyView.visibility = View.VISIBLE
        toggleButton.isEnabled = false
    }

    @Subscribe
    fun on(event: DisableSelectionEmptyStateEvent) {
        emptyView.visibility = View.INVISIBLE
        toggleButton.isEnabled = true
    }

    @Subscribe
    fun on(event: DisableDrawActionsEvent) {
        drawCountSelector.isEnabled = false
        drawButton.isEnabled = false
    }

    @Subscribe
    fun on(event: EnableDrawActionsEvent) {
        drawCountSelector.isEnabled = true
        drawButton.isEnabled = true
    }

    private fun addMenuItem(menuItemResId: Int, runnable: Runnable) {
        val menuItem = menu.findItem(menuItemResId)
        val wrap = DrawableCompat.wrap(menuItem.icon).mutate()
        DrawableCompat.setTint(wrap, ContextCompat.getColor(this, android.R.color.white))
        menuItem.icon = wrap
        menuItem.setOnMenuItemClickListener {
            runnable.run()
            true
        }
    }

    private inner class SelectionAdapter internal constructor(private val context: Context) : BaseAdapter() {

        override fun getCount(): Int = dataWrapper.count()

        override fun getItem(position: Int): Any = dataWrapper.item(position)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val nameSelectionView = if (convertView == null)
                NameSelectionView(context)
                else convertView as NameSelectionView

            nameSelectionView.bind(dataWrapper.item(position),
                object : CheckedChangeListener {
                    override fun onCheckedChanged(isChecked: Boolean) {
                        bus.post(NameSelectionCheckChangedEvent(position, isChecked))}})

            return nameSelectionView
        }
    }

    override val screenName: String
        get() = "Selection Screen"

    companion object {
        private val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun launchIntent(context: Context, groupId: Long): Intent {
            val intent = Intent(context, SelectionActivity::class.java)
            intent.putExtra(EXTRA_GROUP_ID, groupId)
            return intent
        }
    }
}
