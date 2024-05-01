package uk.lobsterdoodle.namepicker.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import androidx.core.app.TaskStackBuilder
import android.widget.Button

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent
import uk.lobsterdoodle.namepicker.databinding.ActivityCreateGroupBinding
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.overview.OverviewActivity
import uk.lobsterdoodle.namepicker.storage.EditGroupDetailsEvent
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.GroupNameEditedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent
import uk.lobsterdoodle.namepicker.ui.FlowActivity

class EditGroupDetailsActivity : FlowActivity() {

    private lateinit var binding: ActivityCreateGroupBinding

    private val editGroupName: TextInputEditText
        get() = binding.createGroupNameInput

    private val done: Button
        get() = binding.createGroupDoneButton

    @Inject
    lateinit var bus: EventBus


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App[this].component().inject(this)

        done.setOnClickListener {
            val event: Any =
                if (!intent.hasExtra(EXTRA_GROUP_ID)) CreateGroupDetailsEvent(editGroupName.text.toString())
                else EditGroupDetailsEvent(
                    intent.getLongExtra(EXTRA_GROUP_ID, -1),
                    editGroupName.text.toString()
                )
            bus.post(event)
        }
    }

    public override fun onResume() {
        super.onResume()
        bus.register(this)
        if (intent.hasExtra(EXTRA_GROUP_ID)) {
            bus.post(RetrieveGroupDetailsEvent(intent.getLongExtra(EXTRA_GROUP_ID, -1)))
        }
    }

    public override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun on(event: GroupDetailsRetrievedSuccessfullyEvent) {
        supportActionBar?.title = "Edit: ${event.details.name}"
        editGroupName.setText(event.details.name)
    }

    @Subscribe
    fun on(event: GroupCreationSuccessfulEvent) {
        TaskStackBuilder.create(this)
            .addNextIntent(OverviewActivity.launchIntent(this))
            .addNextIntent(EditNamesActivity.launchIntent(this, event.groupId))
            .startActivities()
        finish()
    }

    @Subscribe
    fun on(event: GroupNameEditedSuccessfullyEvent) {
        TaskStackBuilder.create(this)
            .addNextIntent(OverviewActivity.launchIntent(this))
            .startActivities()
        finish()
    }

    override val screenName: String
        get() = "Edit Group Details"

    companion object {
        private val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun launchIntent(context: Context): Intent =
            Intent(context, EditGroupDetailsActivity::class.java)

        fun launchIntent(context: Context, groupId: Long): Intent {
            val intent = Intent(context, EditGroupDetailsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_ID, groupId)
            return intent
        }
    }
}
