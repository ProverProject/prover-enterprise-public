package io.prover.common.transport.base;

public interface INetworkRequestBasicListener<T> {
    void onNetworkRequestDone(NetworkRequest request, T responce);
}
