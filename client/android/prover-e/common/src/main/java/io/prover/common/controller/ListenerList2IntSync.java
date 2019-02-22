package io.prover.common.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifies listeners of event. synchronously
 */

public class ListenerList2IntSync<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final NotificationRunner<T> notificationRunner;

    public ListenerList2IntSync(NotificationRunner<T> notificationRunner) {
        this.notificationRunner = notificationRunner;
    }

    public synchronized void add(T listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public synchronized void remove(T listener) {
        listeners.remove(listener);
    }

    void notifyEvent(final int param1, final int param2) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1, param2);
        }
    }

    public interface NotificationRunner<T> {
        void doNotification(T listener, int param1, int param2);
    }
}
