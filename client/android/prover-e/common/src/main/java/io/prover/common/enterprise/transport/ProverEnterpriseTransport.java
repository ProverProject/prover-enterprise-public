package io.prover.common.enterprise.transport;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.prover.common.enterprise.transport.request.EstimateFeeRequest;
import io.prover.common.enterprise.transport.request.GetBalanceRequest;
import io.prover.common.enterprise.transport.request.GetStatusRequest;
import io.prover.common.enterprise.transport.request.ProverEnterpriseRequest;
import io.prover.common.enterprise.transport.request.RequestSwypeCodeRequest;
import io.prover.common.enterprise.transport.request.SubmitMediaHashRequest;
import io.prover.common.enterprise.transport.request.SubmitMessageRequest;
import io.prover.common.enterprise.transport.request.SwypeCodeFastRequest;
import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.enterprise.transport.response.QrCodeReply;
import io.prover.common.enterprise.transport.response.ServerStatus;
import io.prover.common.enterprise.transport.response.SubmitMediaHashReply;
import io.prover.common.enterprise.transport.response.SwypeCodeFastReply;
import io.prover.common.enterprise.transport.response.SwypeCodeReply;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.base.INetworkRequestBasicListener;
import io.prover.common.transport.base.Transport;

public class ProverEnterpriseTransport extends Transport {

    private static volatile ProverEnterpriseTransport instance;

    public static ProverEnterpriseTransport getInstance() {
        ProverEnterpriseTransport local = instance;
        if (local == null) {
            synchronized (ProverEnterpriseTransport.class) {
                local = instance;
                if (local == null) {
                    local = instance = new ProverEnterpriseTransport();
                }
            }
        }
        return local;
    }

    public ProverEnterpriseRequest getServerStatus(Uri server, @NonNull INetworkRequestBasicListener<ServerStatus> listener) {
        return new GetStatusRequest(client, server, new NetworkRequestWrapper<>(listener)).execute();
    }

    public ProverEnterpriseRequest getBalance(Uri server, @NonNull INetworkRequestBasicListener<EnterpriseBalance> listener) {
        return new GetBalanceRequest(client, server, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest estimateRequestFee(Uri server, OrderType orderType, OrderRequestData requestData, INetworkRequestBasicListener<FeeEstimate> listener) {
        return new EstimateFeeRequest(client, server, orderType, requestData, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest requestSwypeCode(Uri server, INetworkRequestBasicListener<SwypeCodeReply> listener) {
        return new RequestSwypeCodeRequest(client, server, null, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest checkSwypeCodeRequest(Uri server, @Nullable SwypeCodeReply firstReply, INetworkRequestBasicListener<SwypeCodeReply> listener) {
        return new RequestSwypeCodeRequest(client, server, firstReply, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest requestSwypeCodeFast(Uri server, INetworkRequestBasicListener<SwypeCodeFastReply> listener) {
        return new SwypeCodeFastRequest(client, server, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest requestQrCode(Uri server, String message, long clientId, INetworkRequestBasicListener<QrCodeReply> listener) {
        return new SubmitMessageRequest(client, server, message, clientId, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest checkQrCodeRequest(Uri server, QrCodeReply firstReply, INetworkRequestBasicListener<QrCodeReply> listener) {
        return new SubmitMessageRequest(client, server, firstReply, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest submitMediaHash(Uri server, MediaHashRequestData request, INetworkRequestBasicListener<SubmitMediaHashReply> listener) {
        return new SubmitMediaHashRequest(client, server, request, new NetworkRequestWrapper<>(listener)).execute();
    }

    ProverEnterpriseRequest checkSubmitMediaHashRequest(Uri server, SubmitMediaHashReply firstReply, INetworkRequestBasicListener<SubmitMediaHashReply> listener) {
        return new SubmitMediaHashRequest(client, server, firstReply, new NetworkRequestWrapper<>(listener)).execute();
    }
}