package io.prover.common.transport.base;

public interface INetworkRequestListener<T> extends INetworkRequestBasicListener<T>, INetworkRequestErrorListener, INetworkRequestStartListener, INetworkRequestCancelListener {
}
