package io.prover.common.enterprise.transport;

import android.net.Uri;

import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkListenerWrapper;
import io.prover.common.transport.base.NetworkRequest;

class FeeEstimater {

    private final ProverEnterpriseTransport transport;
    private final NetworkListenerWrapper<FeeEstimate> networkListener;
    private final EnterpriseTransportModel.OnGotEstimateFeeListener listener;
    private final OrderType orderType;
    private final String qrCodeMessage;
    private final EnterpriseTransportModel.OnGetFeeErrorListener errorListener;
    private final Uri serverUri;
    private FeeEstimate firstEstimate = null;

    public FeeEstimater(Uri serverUri, ProverEnterpriseTransport transport, OrderType orderType, String qrCodeMessage,
                        EnterpriseTransportModel.OnGotEstimateFeeListener listener, EnterpriseTransportModel.OnGetFeeErrorListener errorListener, INetworkRequestListener networkListener) {
        this.serverUri = serverUri;
        this.transport = transport;
        this.networkListener = new NetworkListenerWrapper<FeeEstimate>(networkListener) {
            @Override
            public void onNetworkRequestDone(NetworkRequest request, FeeEstimate responce) {
                super.onNetworkRequestDone(request, responce);
                onGotEstimate(responce);
            }

            @Override
            public void onNetworkRequestError(NetworkRequest request, Exception e) {
                super.onNetworkRequestError(request, e);
                errorListener.onGetFeeError(e);
            }
        };
        this.orderType = orderType;
        this.qrCodeMessage = qrCodeMessage;
        this.listener = listener;
        this.errorListener = errorListener;
    }

    public void start() {
        switch (orderType) {
            case QrCode:
                OrderRequestData data = OrderRequestData.requestQrCode(qrCodeMessage, 0);
                transport.estimateRequestFee(serverUri, orderType, data, networkListener);
                break;

            case SwypeFast:
                transport.estimateRequestFee(serverUri, orderType, null, networkListener);
                break;

            case SwypeFull:
                transport.estimateRequestFee(serverUri, OrderType.SwypeFull, null, networkListener);
                transport.estimateRequestFee(serverUri, OrderType.FileHash, null, networkListener);
                break;
        }
    }

    private void onGotEstimate(FeeEstimate responce) {
        switch (orderType) {
            case QrCode:
            case SwypeFast:
                listener.onGotFeeEstimate(orderType, responce);
                break;

            case SwypeFull:
                synchronized (this) {
                    if (firstEstimate == null)
                        firstEstimate = responce;
                    else {
                        listener.onGotFeeEstimate(orderType, firstEstimate.add(responce));
                    }
                }
        }
    }
}
