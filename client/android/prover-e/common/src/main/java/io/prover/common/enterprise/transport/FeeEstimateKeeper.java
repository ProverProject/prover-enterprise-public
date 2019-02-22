package io.prover.common.enterprise.transport;

import android.net.Uri;

import java.util.Objects;

import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkListenerMultiWrapper;
import io.prover.common.transport.keeper.SimpleItemKeeper;

class FeeEstimateKeeper {

    private final ProverEnterpriseTransport transport;
    private final INetworkRequestListener<FeeEstimate> networkListener;
    private final EnterpriseTransportModel.OnGotEstimateFeeListener listener;
    private final EnterpriseTransportModel.OnGetFeeErrorListener errorListener;
    private final SimpleItemKeeper<FeeEstimate> keeper;
    private final Uri serverUri;
    private OrderType orderType;
    private String qrCodeMessage;

    FeeEstimateKeeper(Uri serverUri, ProverEnterpriseTransport transport, INetworkRequestListener<FeeEstimate> networkListener,
                      EnterpriseTransportModel.OnGotEstimateFeeListener listener,
                      EnterpriseTransportModel.OnGetFeeErrorListener errorListener) {
        this.serverUri = serverUri;
        this.transport = transport;
        this.networkListener = networkListener;
        this.listener = listener;
        this.errorListener = errorListener;
        keeper = new SimpleItemKeeper<>(FeeEstimate.OUTDATE_TIME, this::estimateFee);
    }

    private void estimateFee(INetworkRequestListener<FeeEstimate> listener) {
        INetworkRequestListener[] listeners = new INetworkRequestListener[]{listener, networkListener};
        new FeeEstimater(serverUri, transport, orderType, qrCodeMessage, this.listener, errorListener, new NetworkListenerMultiWrapper(listeners))
                .start();
    }

    void setRequest(OrderType orderType, String qrCodeMessage) {
        if (orderType == this.orderType && Objects.equals(this.qrCodeMessage, qrCodeMessage))
            return;

        this.orderType = orderType;
        this.qrCodeMessage = qrCodeMessage;
        keeper.requestValue(null, true);
    }

    FeeEstimate getFee() {
        return keeper.getValue();
    }

    void forceUpdateFee() {
        keeper.requestValue(null, true);
    }
}
