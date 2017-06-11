package uk.lobsterdoodle.namepicker.events

interface EventBus {

    fun post(event: Any)

    fun postSticky(event: Any)

    fun <T> getStickyEvent(c: Class<T>): T

    fun register(subscriber: Any)

    fun unregister(subscriber: Any)

    fun removeStickyEvent(clazz: Class<*>)

    fun isRegistered(subscriber: Any): Boolean
}
