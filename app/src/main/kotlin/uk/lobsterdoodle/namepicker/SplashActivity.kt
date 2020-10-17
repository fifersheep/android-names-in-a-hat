package uk.lobsterdoodle.namepicker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import uk.lobsterdoodle.namepicker.application.App
import uk.lobsterdoodle.namepicker.application.AppService
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.overview.OverviewActivity
import uk.lobsterdoodle.namepicker.selection.SelectionActivity
import uk.lobsterdoodle.namepicker.storage.GroupActiveEvent
import uk.lobsterdoodle.namepicker.storage.CheckForActiveGroupEvent
import uk.lobsterdoodle.namepicker.storage.GroupNotActiveEvent

class SplashActivity : AppCompatActivity(), ServiceConnection {

    @Inject
    lateinit var bus: EventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App[this].component().inject(this)

        val intent = Intent(applicationContext, AppService::class.java)
        startService(intent)
        bindService(intent, this, Context.BIND_ABOVE_CLIENT)
    }

    override fun onDestroy() {
        unbindService(this)
        super.onDestroy()
    }

    @Subscribe
    fun on(event: GroupActiveEvent) {
        bus.unregister(this)
        startActivity(SelectionActivity.launchIntent(this, event.groupId))
        finish()
    }

    @Subscribe
    fun on(event: GroupNotActiveEvent) {
        bus.unregister(this)
        startActivity(OverviewActivity.launchIntent(this))
        finish()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        bus.register(this)
        bus.post(CheckForActiveGroupEvent())
    }

    override fun onServiceDisconnected(name: ComponentName) {}
}
