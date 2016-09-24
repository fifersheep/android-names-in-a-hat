package uk.lobsterdoodle.namepicker.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubbedEventBus implements EventBus {
    public Object lastPostedEvent;
    private List subscribers = new ArrayList();
    private Map<Class, Object> stickyEvents = new HashMap<>();

    @Override
    public void post(Object event) {
        internalPost(event);
    }

    @Override
    public void postSticky(Object event) {
        stickyEvents.put(event.getClass(), event);
        internalPost(event);
    }

    @Override
    public <T> T getStickyEvent(Class<T> eventType) {
        return eventType.cast(stickyEvents.get(eventType));
    }

    @Override
    public void register(Object subscriber) {
        if (!isRegistered(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    @Override
    public void unregister(Object subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void removeStickyEvent(Class event) {
        if (stickyEvents.containsKey(event)) {
            stickyEvents.remove(event);
        }
    }

    @Override
    public boolean isRegistered(Object subscriber) {
        return subscribers.contains(subscriber);
    }

    private void internalPost(Object event) {
        lastPostedEvent = event;

        // Whilst one could argue it leads to unpredictable behaviour, we want to allow subscription during posts.
        // We'll generate concurrent modification exceptions if we do this while using
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscribers.size(); i++) {
            Object subscriber = subscribers.get(i);
            Method method = null;
            try {
                method = subscriber.getClass().getMethod("onEvent", event.getClass());
            } catch (NoSuchMethodException e) {
                try {
                    method = subscriber.getClass().getMethod("onEventMainThread", event.getClass());
                } catch (NoSuchMethodException ignored) {
                    try {
                        method = subscriber.getClass().getMethod("onEventBackgroundThread", event.getClass());
                    } catch (NoSuchMethodException e1) {
                    }
                }
            }

            if (method != null) {
                method.setAccessible(true);
                try {
                    method.invoke(subscriber, event);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // We don't want this to catch runtime exceptions (e.g. NPEs) thrown up from event handling.
                    // Makes testing null handling impossible via events
                    final Throwable targetException = e.getTargetException();
                    if (targetException instanceof RuntimeException) {
                        throw (RuntimeException) targetException;
                    }
                }
            }
        }
    }
}
