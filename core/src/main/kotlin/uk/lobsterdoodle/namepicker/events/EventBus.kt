package uk.lobsterdoodle.namepicker.events

interface EventBus {

    fun post(event: Any)

    fun postSticky(event: Any)

    fun <T> getStickyEvent(clazz: Class<T>): T

    fun register(subscriber: Any)

    fun unregister(subscriber: Any)

    fun <T> removeStickyEvent(clazz: Class<T>)

    fun isRegistered(subscriber: Any): Boolean
}
