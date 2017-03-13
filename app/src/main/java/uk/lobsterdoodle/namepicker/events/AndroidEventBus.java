package uk.lobsterdoodle.namepicker.events;

import android.util.Log;

import uk.lobsterdoodle.namepicker.BuildConfig;

public class AndroidEventBus implements EventBus {

    private final org.greenrobot.eventbus.EventBus eventBus = org.greenrobot.eventbus.EventBus.getDefault();

    @Override
    public void post(Object event) {
        if (BuildConfig.DEBUG) {
            Log.v("EventBus", event.toString());
        }
        eventBus.post(event);
    }

    @Override
    public void postSticky(Object event) {
        eventBus.postSticky(event);
    }

    @Override
    public <T> T getStickyEvent(Class<T> eventType) {
        return eventBus.getStickyEvent(eventType);
    }

    @Override
    public void register(Object subscriber) {
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber);
        }
    }

    @Override
    public void unregister(Object subscriber) {
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber);
        }
    }

    @Override
    public void removeStickyEvent(Class clazz) {
        eventBus.removeStickyEvent(clazz);
    }

    @Override
    public boolean isRegistered(Object subscriber) {
        return eventBus.isRegistered(subscriber);
    }
}
