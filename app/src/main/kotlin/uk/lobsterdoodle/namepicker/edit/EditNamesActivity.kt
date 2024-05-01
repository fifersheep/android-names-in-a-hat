package uk.lobsterdoodle.namepicker.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button

import org.greenrobot.eventbus.Subscribe

import java.util.ArrayList

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.addgroup.AddNameToGroupEvent
import uk.lobsterdoodle.namepicker.addgroup.NameCard
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.databinding.ActivityEditNamesBinding
import uk.lobsterdoodle.namepicker.databinding.ActivityOverviewBinding
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

    private lateinit var binding: ActivityEditNamesBinding

    private val root: ViewGroup
        get() = binding.editNamesRootLayout

    private val nameList: RecyclerView
        get() = binding.editGroupNamesList

    private val addName: Button
        get() = binding.editGroupNamesButtonAddName

    private val done: Button
        get() = binding.editGroupNamesDoneButton

    private val nameInput: TextInputEditText
        get() = binding.editGroupNamesInput

    @Inject
    lateinit var bus: EventBus

    private lateinit var nameListAdapter: NameListAdapter

    private var groupId: Long = 0
    private var cellData: List<Name> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditNamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App.get(this).component().inject(this)

        groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1L)
        nameListAdapter = NameListAdapter()
        nameList.layoutManager = LinearLayoutManager(this)
        nameList.adapter = nameListAdapter
        addName.setOnClickListener {
            bus.post(
                AddNameToGroupEvent(
                    groupId,
                    nameInput.text.toString()
                )
            )
        }
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            NameCardViewHolder(NameCard(this@EditNamesActivity))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val cardViewHolder = holder as NameCardViewHolder
            cardViewHolder.bind(this@EditNamesActivity, cellData[position])
        }

        override fun getItemCount(): Int = cellData.size
    }

    inner class NameCardViewHolder internal constructor(var view: NameCard) :
        RecyclerView.ViewHolder(view) {

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
