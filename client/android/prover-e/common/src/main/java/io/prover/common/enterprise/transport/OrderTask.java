package io.prover.common.enterprise.transport;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.ProverApplication;
import io.prover.common.enterprise.transport.response.IPendingReply;
import io.prover.common.enterprise.transport.response.QrCodeReply;
import io.prover.common.enterprise.transport.response.SubmitMediaHashReply;
import io.prover.common.enterprise.transport.response.SwypeCodeReply;
import io.prover.common.transport.IPostFileHashOrderResult;
import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.TransportModel;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkRequest;

class OrderTask {
    private static final String KEY_DATA = "d";

    private static final int DELAY = 10_000;
    private static final int DELAY_SMALL = 2_000;
    private static final String KEY_SERVER_URI = "s";
    private static final String KEY_FIRST_REPLY = "f";
    private static final String KEY_START_TIMESTAMP = "st";
    public final Uri serverUri;
    final OrderRequestData data;
    private final OrderTaskListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TransportModel.NetworkListener networkListener;
    private final ProverEnterpriseTransport transport;
    private final RequestListener requestListener = new RequestListener();
    private long startTimestamp;
    private long endTimestamp;
    private boolean cancelled = false;
    private boolean requestPosted = false;
    private boolean requestRunning;
    private IPendingReply firstReply;

    public OrderTask(Uri serverUri, OrderTaskListener listener, OrderRequestData requestData, TransportModel.NetworkListener networkListener, ProverEnterpriseTransport transport) {
        this.serverUri = serverUri;
        this.listener = listener;
        this.networkListener = networkListener;
        this.transport = transport;
        this.data = requestData;
    }

    public OrderTask(JSONObject source, OrderTaskListener listener, TransportModel.NetworkListener networkListener, ProverEnterpriseTransport transport) throws JSONException {
        this.listener = listener;
        this.networkListener = networkListener;
        this.transport = transport;

        serverUri = Uri.parse(source.getString(KEY_SERVER_URI));
        data = new OrderRequestData(source.getJSONObject(KEY_DATA));
        startTimestamp = source.optLong(KEY_START_TIMESTAMP, System.currentTimeMillis());

        if (!source.isNull(KEY_FIRST_REPLY)) {
            firstReply = new QrCodeReply(source.getJSONObject(KEY_FIRST_REPLY), null, null);
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put(KEY_DATA, data.toJson());
        jso.put(KEY_SERVER_URI, serverUri.toString());
        jso.put(KEY_START_TIMESTAMP, startTimestamp);
        if (firstReply instanceof IQrCodeOrderResult)
            jso.put(KEY_FIRST_REPLY, ((IQrCodeOrderResult) firstReply).toJson());
        return jso;
    }

    public OrderTask start() {
        startTimestamp = System.currentTimeMillis();
        doRequest();
        return this;
    }

    private void doRequest() {
        switch (data.type) {
            case QrCode:
                transport.requestQrCode(serverUri, data.message, data.clientId, requestListener);
                break;

            case SwypeFull:
                transport.requestSwypeCode(serverUri, requestListener);
                break;

            case SwypeFast:
                transport.requestSwypeCodeFast(serverUri, requestListener);
                break;

            case FileHash:
                transport.submitMediaHash(serverUri, data.toMediaRequestData(), requestListener);
                break;

            default:
                throw new RuntimeException("Not implemented for: " + data.type.name());
        }
    }

    private void checkRequestComplete() {
        switch (data.type) {
            case QrCode:
                transport.checkQrCodeRequest(serverUri, (QrCodeReply) firstReply, requestListener);
                break;

            case SwypeFull:
                transport.checkSwypeCodeRequest(serverUri, (SwypeCodeReply) firstReply, requestListener);
                break;

            case SwypeFast:
                listener.onSwypeCodeOrderComplete(this, (ISwypeCodeOrderResult) firstReply);
                break;

            case FileHash:
                transport.checkSubmitMediaHashRequest(serverUri, (SubmitMediaHashReply) firstReply, requestListener);
                break;

            default:
                throw new RuntimeException("Not implemented for: " + data.type.name());
        }
    }

    private void postRunCheck() {
        requestPosted = true;
        handler.postDelayed(() -> {
            requestPosted = false;
            ProverApplication app = ProverApplication.getApp();
            if (!cancelled && app != null && app.isMainActivityActive()) {
                checkRequestComplete();
            }
        }, getDelay());
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isRequestRunningNow() {
        return requestRunning;
    }

    public long getTimeTookToCompleteRequest() {
        return endTimestamp <= startTimestamp ? 0 : endTimestamp - startTimestamp;
    }

    private long getDelay() {
        return System.currentTimeMillis() - startTimestamp < 10_000 ? DELAY_SMALL : DELAY;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void resume() {
        if (!requestPosted)
            postRunCheck();
    }

    public interface OrderTaskListener {

        void onQrCodeOrderComplete(OrderTask orderTask, IQrCodeOrderResult result);

        void onSwypeCodeOrderComplete(OrderTask orderTask, ISwypeCodeOrderResult result);

        void onPostFileHashOrderComplete(OrderTask orderTask, IPostFileHashOrderResult result);

        void onOrderRequestFailed(OrderTask orderTask, Exception e);

        void onOrderConfirmed(OrderTask orderTask);
    }

    private class RequestListener implements INetworkRequestListener {
        @Override
        public void onNetworkRequestDone(NetworkRequest request, Object responce) {
            requestRunning = false;
            networkListener.onNetworkRequestDone(request, responce);

            if (responce instanceof IPendingReply) {
                if (firstReply == null) {
                    firstReply = (IPendingReply) responce;
                    listener.onOrderConfirmed(OrderTask.this);
                }

                if (((IPendingReply) responce).isStillPending()) {
                    postRunCheck();
                } else {
                    endTimestamp = System.currentTimeMillis();

                    if (responce instanceof IQrCodeOrderResult)
                        listener.onQrCodeOrderComplete(OrderTask.this, (IQrCodeOrderResult) responce);
                    else if (responce instanceof ISwypeCodeOrderResult)
                        listener.onSwypeCodeOrderComplete(OrderTask.this, (ISwypeCodeOrderResult) responce);
                    else if (responce instanceof SubmitMediaHashReply)
                        listener.onPostFileHashOrderComplete(OrderTask.this, (IPostFileHashOrderResult) responce);
                }
            }
        }

        @Override
        public void onNetworkRequestCancel(NetworkRequest request) {
            networkListener.onNetworkRequestCancel(request);
            endTimestamp = System.currentTimeMillis();
            switch (data.type) {
                case QrCode:
                    listener.onQrCodeOrderComplete(OrderTask.this, null);
                    break;

                case SwypeFull:
                case SwypeFast:
                    listener.onSwypeCodeOrderComplete(OrderTask.this, null);
                    break;

                case FileHash:
                    listener.onPostFileHashOrderComplete(OrderTask.this, null);
                    break;
            }
            requestRunning = false;
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            networkListener.onNetworkRequestError(request, e);
            listener.onOrderRequestFailed(OrderTask.this, e);
            requestRunning = false;
        }

        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            networkListener.onNetworkRequestStart(request);
            requestRunning = true;
        }
    }
}
