package uk.lobsterdoodle.namepicker.events;

public interface EventBus {

    void post(Object event);

    void postSticky(Object event);

    <T> T getStickyEvent(Class<T> c);

    void register(Object subscriber);

    void unregister(Object subscriber);

    void removeStickyEvent(Class clazz);

    boolean isRegistered(Object subscriber);
}
