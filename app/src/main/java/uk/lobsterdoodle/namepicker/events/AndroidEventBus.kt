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

    override fun <T> getStickyEvent(eventType: Class<T>): T
            = eventBus.getStickyEvent(eventType)

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

    override fun removeStickyEvent(clazz: Class<Any>) {
        eventBus.removeStickyEvent<Any>(clazz)
    }

    override fun isRegistered(subscriber: Any): Boolean
            = eventBus.isRegistered(subscriber)
}
