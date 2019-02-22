package io.prover.common.transport.base;

public interface INetworkRequestErrorListener {
    void onNetworkRequestError(NetworkRequest request, Exception e);
}
