package io.prover.common.transport.keeper;

import java.util.ArrayList;
import java.util.List;

import io.prover.common.transport.base.INetworkRequestBasicListener;
import io.prover.common.transport.base.INetworkRequestCancelListener;
import io.prover.common.transport.base.INetworkRequestErrorListener;
import io.prover.common.transport.base.INetworkRequestStartListener;
import io.prover.common.transport.base.NetworkRequest;

public class NetworkCallbacks<T> {

    private final List<ISimpleGetObjectCallback<T>> callbacks = new ArrayList<>();

    public void add(ISimpleGetObjectCallback<T> callback) {
        if (callback != null && !callbacks.contains(callback))
            callbacks.add(callback);
    }

    public void remove(ISimpleGetObjectCallback<T> callback) {
        callbacks.remove(callback);
    }

    public void clear() {
        callbacks.clear();
    }

    public void callRequestStart(NetworkRequest request) {
        for (int i = 0; i < callbacks.size(); i++) {
            ISimpleGetObjectCallback<T> callback = callbacks.get(i);
            if (callback instanceof INetworkRequestStartListener) {
                ((INetworkRequestStartListener) callback).onNetworkRequestStart(request);
            }
        }
    }

    public void callResult(NetworkRequest request, T result) {
        for (int i = 0; i < callbacks.size(); i++) {
            ISimpleGetObjectCallback<T> callback = callbacks.get(i);
            if (callback instanceof INetworkRequestBasicListener)
                ((INetworkRequestBasicListener) callback).onNetworkRequestDone(request, result);
            callback.onRequestResult(result);
        }
    }

    public void callError(NetworkRequest request, Exception error) {
        for (int i = 0; i < callbacks.size(); i++) {
            ISimpleGetObjectCallback<T> callback = callbacks.get(i);
            if (callback instanceof INetworkRequestErrorListener) {
                ((INetworkRequestErrorListener) callback).onNetworkRequestError(request, error);
            }
        }
    }

    public void callCancelled(NetworkRequest request) {
        for (int i = 0; i < callbacks.size(); i++) {
            ISimpleGetObjectCallback<T> callback = callbacks.get(i);
            if (callback instanceof INetworkRequestCancelListener) {
                ((INetworkRequestCancelListener) callback).onNetworkRequestCancel(request);
            }
        }
    }
}
