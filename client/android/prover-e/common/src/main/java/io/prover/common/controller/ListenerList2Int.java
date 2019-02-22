package io.prover.common.controller;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifies listeners of event. asynchronously (on specified handler thread)
 */

public class ListenerList2Int<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T> notificationRunner;

    public ListenerList2Int(Handler handler, NotificationRunner<T> notificationRunner) {
        this.handler = handler;
        this.notificationRunner = notificationRunner;
    }

    public synchronized void add(T listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public synchronized void remove(T listener) {
        listeners.remove(listener);
    }

    public void postNotifyEvent(final int param1, final int param2) {
        handler.post(() -> notifyEvent(param1, param2));
    }

    private void notifyEvent(final int param1, final int param2) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1, param2);
        }
    }

    public interface NotificationRunner<T> {
        void doNotification(T listener, int param1, int param2);
    }
}
