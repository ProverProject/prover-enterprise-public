package io.prover.common.transport.keeper;

import io.prover.common.transport.base.INetworkRequestCancelListener;
import io.prover.common.transport.base.INetworkRequestErrorListener;
import io.prover.common.transport.base.INetworkRequestStartListener;

public interface IGetObjectCallback<T> extends ISimpleGetObjectCallback<T>, INetworkRequestErrorListener, INetworkRequestCancelListener, INetworkRequestStartListener {
    void onRequestResult(T responce);
}
