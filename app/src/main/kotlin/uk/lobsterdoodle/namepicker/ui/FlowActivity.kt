package uk.lobsterdoodle.namepicker.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.events.EventBus

abstract class FlowActivity : AppCompatActivity() {

    @Inject lateinit var eventBus: EventBus

    internal var orientations = mapOf(
            Configuration.ORIENTATION_UNDEFINED to "Undefined",
            Configuration.ORIENTATION_PORTRAIT to "Portrait",
            Configuration.ORIENTATION_LANDSCAPE to "Landscape")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.flow_activity_open_enter, R.anim.flow_activity_open_exit)
        App.get(this).component().inject(this)
        eventBus.post(ScreenLaunchedEvent(screenName, orientations[resources.configuration.orientation]!!))
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.flow_activity_close_enter, R.anim.flow_activity_close_exit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    abstract val screenName: String
}
