package uk.lobsterdoodle.namepicker.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.databinding.ActivityOverviewBinding
import uk.lobsterdoodle.namepicker.edit.EditGroupDetailsActivity
import uk.lobsterdoodle.namepicker.edit.EditNamesActivity
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.selection.SelectionActivity
import uk.lobsterdoodle.namepicker.storage.ClearActiveGroupEvent
import uk.lobsterdoodle.namepicker.storage.DeleteGroupEvent
import uk.lobsterdoodle.namepicker.storage.GroupDeletedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.ui.OverviewCard
import java.util.*
import javax.inject.Inject

class OverviewActivity : AppCompatActivity(), OverviewCardActionsCallback {

    private lateinit var binding: ActivityOverviewBinding

    private val root: ViewGroup
        get() = binding.overviewRootLayout

    private val groupsRecyclerView: RecyclerView
        get() = binding.overviewGroupList

    private val addGroupButton: Button
        get() = binding.overviewAddGroup

    private val hintArrow: ImageView
        get() = binding.overviewHintArrow

    private val hintText: TextView
        get() = binding.overviewHintText

    @Inject
    lateinit var bus: EventBus

    private lateinit var overviewAdapter: OverviewAdapter
    private var cellData: List<OverviewCardCellData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App[this].component().inject(this)

        supportActionBar?.title = getString(R.string.title_activity_overview)

        overviewAdapter = OverviewAdapter()
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsRecyclerView.adapter = overviewAdapter
        addGroupButton.setOnClickListener { startActivity(EditGroupDetailsActivity.launchIntent(this)) }

    }

    public override fun onResume() {
        super.onResume()
        bus.register(this)
        bus.post(OverviewBecameVisibleEvent())
        bus.post(ClearActiveGroupEvent())
    }

    public override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEvent(retrievedEvent: OverviewRetrievedEvent) {
        cellData = retrievedEvent.cellData
        runOnUiThread { overviewAdapter.notifyDataSetChanged() }
    }

    @Subscribe
    fun on(event: GroupDeletedSuccessfullyEvent) {
        bus.post(OverviewBecameVisibleEvent())
        Snackbar.make(root, "${event.groupName} deleted", Snackbar.LENGTH_SHORT).show()
    }

    @Subscribe
    fun on(event: ShowAddGroupHintEvent) {
        hintArrow.visibility = View.VISIBLE
        hintText.visibility = View.VISIBLE
    }

    override fun launchEditGroupNamesScreen(groupId: Long) =
        startActivity(EditNamesActivity.launchIntent(this, groupId))

    override fun launchEditGroupDetailsScreen(groupId: Long) =
        startActivity(EditGroupDetailsActivity.launchIntent(this, groupId))

    override fun deleteGroup(groupId: Long) = bus.post(DeleteGroupEvent(groupId))

    override fun launchSelectionScreen(groupId: Long) =
        startActivity(SelectionActivity.launchIntent(this, groupId))

    private inner class OverviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            OverviewCardViewHolder(OverviewCard(this@OverviewActivity))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder as OverviewCardViewHolder).bind(cellData[position])

        override fun getItemCount(): Int = cellData.size
    }

    private inner class OverviewCardViewHolder internal constructor(var view: OverviewCard) :
        RecyclerView.ViewHolder(view) {
        internal fun bind(data: OverviewCardCellData) = view.bind(this@OverviewActivity, data)
    }

    companion object {
        fun launchIntent(context: Context): Intent = Intent(context, OverviewActivity::class.java)
    }
}