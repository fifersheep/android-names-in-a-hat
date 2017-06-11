package uk.lobsterdoodle.namepicker.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button

import org.greenrobot.eventbus.Subscribe

import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent
import uk.lobsterdoodle.namepicker.addgroup.NameCard
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.namelist.RetrieveGroupNamesEvent
import uk.lobsterdoodle.namepicker.storage.DeleteNameEvent
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent
import uk.lobsterdoodle.namepicker.storage.NameAddedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.NameDeletedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent
import uk.lobsterdoodle.namepicker.ui.FlowActivity

class EditNamesActivity : FlowActivity(), NameCardActions {

    @BindView(R.id.edit_names_root_layout)
    lateinit var root: ViewGroup

    @BindView(R.id.edit_group_names_list)
    lateinit var nameList: RecyclerView

    @BindView(R.id.edit_group_names_button_add_name)
    lateinit var addName: Button

    @BindView(R.id.edit_group_names_done_button)
    lateinit var done: Button

    @BindView(R.id.edit_group_names_input)
    lateinit var nameInput: TextInputEditText

    @Inject lateinit var bus: EventBus

    private lateinit var nameListAdapter: NameListAdapter

    private var groupId: Long = 0
    private var cellData: List<Name> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_names)
        App.get(this).component().inject(this)
        ButterKnife.bind(this)

        groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1L)
        nameListAdapter = NameListAdapter()
        nameList.layoutManager = LinearLayoutManager(this)
        nameList.adapter = nameListAdapter
        addName.setOnClickListener { bus.post(AddNameToGroupEvent(groupId, nameInput.text.toString())) }
        done.setOnClickListener { finish() }
    }

    public override fun onResume() {
        super.onResume()
        bus.register(this)
        bus.post(RetrieveGroupNamesEvent(groupId))
        bus.post(RetrieveGroupDetailsEvent(groupId))
    }

    public override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun on(event: GroupDetailsRetrievedSuccessfullyEvent) {
        if (supportActionBar != null)
            supportActionBar!!.title = "Names: ${event.details.name}"
    }

    @Subscribe
    fun on(event: GroupNamesRetrievedEvent) {
        cellData = event.names
        runOnUiThread { nameListAdapter.notifyDataSetChanged() }
    }

    @Subscribe
    fun on(event: NameAddedSuccessfullyEvent) {
        nameInput.setText("")
    }

    @Subscribe
    fun on(event: NameDeletedSuccessfullyEvent) {
        bus.post(RetrieveGroupNamesEvent(groupId))
        Snackbar.make(root, "${event.name} deleted", Snackbar.LENGTH_SHORT).show()
    }

    override fun deleteName(nameId: Long) = bus.post(DeleteNameEvent(groupId, nameId))

    inner class NameListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                = NameCardViewHolder(NameCard(this@EditNamesActivity))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val cardViewHolder = holder as NameCardViewHolder
            cardViewHolder.bind(this@EditNamesActivity, cellData[position])
        }

        override fun getItemCount(): Int = cellData.size
    }

    inner class NameCardViewHolder internal constructor(var view: NameCard) : RecyclerView.ViewHolder(view) {

        internal fun bind(nameCardActions: NameCardActions, data: Name) {
            view.bind(nameCardActions, data)
        }
    }

    override val screenName: String
        get() = "Edit Names Screen"

    companion object {
        val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun launchIntent(context: Context, groupId: Long): Intent {
            val intent = Intent(context, EditNamesActivity::class.java)
            intent.putExtra(EditNamesActivity.EXTRA_GROUP_ID, groupId)
            return intent
        }
    }
}
