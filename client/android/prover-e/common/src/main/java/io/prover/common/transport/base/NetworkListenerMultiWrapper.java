package io.prover.common.transport.base;

public class NetworkListenerMultiWrapper<T> implements INetworkRequestListener<T> {

    private final INetworkRequestListener<T>[] listeners;

    public NetworkListenerMultiWrapper(INetworkRequestListener<T>[] listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, T responce) {
        for (INetworkRequestListener<T> listener : listeners) {
            listener.onNetworkRequestDone(request, responce);
        }
    }

    @Override
    public void onNetworkRequestCancel(NetworkRequest request) {
        for (INetworkRequestListener<T> listener : listeners) {
            listener.onNetworkRequestCancel(request);
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        for (INetworkRequestListener<T> listener : listeners) {
            listener.onNetworkRequestError(request, e);
        }
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        for (INetworkRequestListener<T> listener : listeners) {
            listener.onNetworkRequestStart(request);
        }
    }
}
