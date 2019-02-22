package io.prover.common.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifies listeners of event. synchronously
 */
public class ListenerList1Sync<T, Q> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final NotificationRunner<T, Q> notificationRunner;

    public ListenerList1Sync(NotificationRunner<T, Q> notificationRunner) {
        this.notificationRunner = notificationRunner;
    }

    public synchronized void add(T listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public synchronized void remove(T listener) {
        listeners.remove(listener);
    }

    public void notifyEvent(final Q param1) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1);
        }
    }

    public interface NotificationRunner<T, Q> {
        void doNotification(T listener, Q param1);
    }
}
