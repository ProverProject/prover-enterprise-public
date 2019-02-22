package io.prover.common.transport.keeper;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkRequest;

@UiThread
public class SimpleItemKeeper<T> implements ISimpleItemKeeper<T>, INetworkRequestListener<T> {

    private final NetworkCallbacks<T> dynamicCallbacks = new NetworkCallbacks<>();
    private final NetworkCallbacks<T> updateListeners = new NetworkCallbacks<>();
    private final long valueTimeout;
    private final Executor<T> executor;
    protected T value;
    protected int requestCounter;
    protected long requestStarttimestamp;
    private long responseTimestamp;

    public SimpleItemKeeper(long valueTimeout, Executor<T> executor) {
        this.valueTimeout = valueTimeout;
        this.executor = executor;
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        dynamicCallbacks.callRequestStart(request);
        updateListeners.callRequestStart(request);
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, T responce) {
        value = responce;
        responseTimestamp = System.currentTimeMillis();
        --requestCounter;
        dynamicCallbacks.callResult(request, responce);
        dynamicCallbacks.clear();
        updateListeners.callResult(request, responce);
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        --requestCounter;
        dynamicCallbacks.callError(request, e);
        dynamicCallbacks.clear();
        updateListeners.callError(request, e);
    }

    @Override
    public void onNetworkRequestCancel(NetworkRequest request) {
        --requestCounter;
        dynamicCallbacks.callCancelled(request);
        dynamicCallbacks.clear();
        updateListeners.callCancelled(request);
    }

    public long getResponseTimestamp() {
        return responseTimestamp;
    }

    @Override
    public boolean isOutdated() {
        return value == null || System.currentTimeMillis() - responseTimestamp > valueTimeout;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void requestValue(@Nullable final ISimpleGetObjectCallback<T> listener, boolean forceUpdate) {
        if (requestCounter > 0) {
            dynamicCallbacks.add(listener);
        } else {
            if (forceUpdate || isOutdated()) {
                dynamicCallbacks.add(listener);
                requestStarttimestamp = System.currentTimeMillis();
                doRequest();
            }
        }

        if (listener != null && value != null && !forceUpdate) {
            listener.onRequestResult(value);
        }
    }

    public void addListener(ISimpleGetObjectCallback<T> listener) {
        updateListeners.add(listener);
        if (value != null)
            listener.onRequestResult(value);
    }

    public void removeListener(ISimpleGetObjectCallback<T> listener) {
        updateListeners.remove(listener);
    }

    @Override
    public void clear() {
        value = null;
    }

    protected void doRequest() {
        ++requestCounter;
        executor.executeRequest(this);
    }

    public interface Executor<T> {
        void executeRequest(INetworkRequestListener<T> listener);
    }
}
