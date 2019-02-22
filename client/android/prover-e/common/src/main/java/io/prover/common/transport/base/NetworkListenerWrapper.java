package io.prover.common.transport.base;

public class NetworkListenerWrapper<T> implements INetworkRequestListener<T> {

    private final INetworkRequestListener<T> parent;

    public NetworkListenerWrapper(INetworkRequestListener<T> parent) {
        this.parent = parent;
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, T responce) {
        parent.onNetworkRequestDone(request, responce);
    }

    @Override
    public void onNetworkRequestCancel(NetworkRequest request) {
        parent.onNetworkRequestCancel(request);
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        parent.onNetworkRequestError(request, e);
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        parent.onNetworkRequestStart(request);
    }
}
