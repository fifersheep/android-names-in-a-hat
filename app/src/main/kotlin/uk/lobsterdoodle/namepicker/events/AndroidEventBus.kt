package uk.lobsterdoodle.namepicker.events

import android.util.Log

import uk.lobsterdoodle.namepicker.BuildConfig

class AndroidEventBus : EventBus {

    private val eventBus = org.greenrobot.eventbus.EventBus.getDefault()

    override fun post(event: Any) {
        if (BuildConfig.DEBUG) Log.v("EventBus", event.toString())
        eventBus.post(event)
    }

    override fun postSticky(event: Any)
            = eventBus.postSticky(event)

    override fun <T> getStickyEvent(clazz: Class<T>): T
            = eventBus.getStickyEvent(clazz)

    override fun register(subscriber: Any) {
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber)
        }
    }

    override fun unregister(subscriber: Any) {
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber)
        }
    }

    override fun <T> removeStickyEvent(clazz: Class<T>) {
        eventBus.removeStickyEvent<T>(clazz)
    }

    override fun isRegistered(subscriber: Any): Boolean
            = eventBus.isRegistered(subscriber)
}
