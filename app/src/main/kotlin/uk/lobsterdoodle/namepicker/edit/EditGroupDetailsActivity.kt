package uk.lobsterdoodle.namepicker.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.TaskStackBuilder
import android.widget.Button

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.creategroup.CreateGroupDetailsEvent
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.overview.OverviewActivity
import uk.lobsterdoodle.namepicker.storage.EditGroupDetailsEvent
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent
import uk.lobsterdoodle.namepicker.storage.GroupDetailsRetrievedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.GroupNameEditedSuccessfullyEvent
import uk.lobsterdoodle.namepicker.storage.RetrieveGroupDetailsEvent
import uk.lobsterdoodle.namepicker.ui.FlowActivity

class EditGroupDetailsActivity : FlowActivity() {

    @BindView(R.id.create_group_name_input)
    lateinit var editGroupName: TextInputEditText

    @BindView(R.id.create_group_done_button)
    lateinit var done: Button

    @Inject lateinit var bus: EventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        App[this].component().inject(this)
        ButterKnife.bind(this)

        done.setOnClickListener {
            val event: Any = if (!intent.hasExtra(EXTRA_GROUP_ID)) CreateGroupDetailsEvent(editGroupName.text.toString())
            else EditGroupDetailsEvent(intent.getLongExtra(EXTRA_GROUP_ID, -1), editGroupName.text.toString())
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

        fun launchIntent(context: Context): Intent
                = Intent(context, EditGroupDetailsActivity::class.java)

        fun launchIntent(context: Context, groupId: Long): Intent {
            val intent = Intent(context, EditGroupDetailsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_ID, groupId)
            return intent
        }
    }
}
