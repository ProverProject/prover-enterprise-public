package io.prover.common.enterprise.transport;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.BuildConfig;
import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList2;
import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.transport.IPostFileHashOrderResult;
import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.TransportModel;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkListenerMultiWrapper;
import io.prover.common.transport.base.NetworkRequest;
import io.prover.common.transport.keeper.IGetObjectCallback;
import io.prover.common.transport.keeper.ISimpleItemKeeper;
import io.prover.common.transport.keeper.SimpleItemKeeper;

import static io.prover.common.Const.ARG_ORDER_REQUEST;
import static io.prover.common.Const.TAG;

public class EnterpriseTransportModel extends TransportModel {

    public final ListenerList2<OnGotEstimateFeeListener, OrderType, FeeEstimate> onFeeEstimateUpdateListener =
            new ListenerList2<>(handler, OnGotEstimateFeeListener::onGotFeeEstimate);

    public final ListenerList1<OnGetFeeErrorListener, Exception> onGetFeeErrorListener =
            new ListenerList1<>(handler, OnGetFeeErrorListener::onGetFeeError);

    public final ListenerList1<OnBalanceUpdateListener, EnterpriseBalance> onBalanceUpdateListener =
            new ListenerList1<>(handler, OnBalanceUpdateListener::onBalanceUpdate);

    private final ProverEnterpriseTransport transport;
    private final FeeEstimateKeeper feeEstimateKeeper;
    private final NetworkListenerLocal networkListener = new NetworkListenerLocal();
    private final Uri serverUri;
    private final ISimpleItemKeeper<EnterpriseBalance> balanceKeeper = new SimpleItemKeeper<>(300_000, this::getBalance);
    private int clientId;
    private OrderTask orderTask;
    private OrderType orderType;

    public EnterpriseTransportModel(Uri server, int clientId, OrderType orderType) {
        transport = new ProverEnterpriseTransport();
        this.serverUri = server;
        feeEstimateKeeper = new FeeEstimateKeeper(serverUri, transport, networkListener, onFeeEstimateUpdateListener::postNotifyEvent, onGetFeeErrorListener::postNotifyEvent);
        balanceKeeper.addListener(onBalanceUpdateListener::postNotifyEvent);
        this.clientId = clientId;
        this.orderType = orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void estimateSwypeIdFee() {
        feeEstimateKeeper.setRequest(orderType, null);
        updateBalance(false);
    }

    public void estimateQrCodeFee(String qrCodeMessage) {
        feeEstimateKeeper.setRequest(OrderType.QrCode, qrCodeMessage);
        updateBalance(false);
    }


    public void updateFeeEstimate(boolean force) {
        if (force) {
            feeEstimateKeeper.forceUpdateFee();
        } else {
            FeeEstimate fee = feeEstimateKeeper.getFee();
            if (fee != null) {
                onFeeEstimateUpdateListener.postNotifyEvent(OrderType.QrCode, fee);
            }
        }
    }

    public void updateBalance(boolean force) {
        balanceKeeper.requestValue(null, force);
    }

    @Override
    public void setBeReadyToWork(boolean ready) {
        setBeReadyToStart(ready);
    }

    @Override
    public void setBeReadyToStart(boolean ready) {
        if (ready) {
            updateBalance(true);
            if (orderType == OrderType.QrCode)
                estimateQrCodeFee("");
            else
                estimateSwypeIdFee();
        }
    }

    @Override
    public boolean isReadyForMainTask() {
        return true;
    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public boolean requestQrCode(@NonNull String message) {
        OrderRequestData data = OrderRequestData.requestQrCode(message, clientId);
        orderTask = new OrderTask(serverUri, new OrderTaskListener(), data, networkListener, transport).start();
        return true;
    }

    @Override
    public boolean requestSwypeCode() {
        OrderRequestData data = OrderRequestData.requestSwypeCode(orderType == OrderType.SwypeFast);
        orderTask = new OrderTask(serverUri, new OrderTaskListener(), data, networkListener, transport).start();

        return true;
    }

    @Override
    public boolean postFileHash(@NonNull ISwypeCodeOrderResult swypeOrder, @NonNull byte[] digest) {
        OrderRequestData data = OrderRequestData.submitFileHash(digest, swypeOrder, clientId);
        new OrderTask(serverUri, new OrderTaskListener(), data, networkListener, transport).start();
        return true;
    }

    @Override
    public boolean postFileHashNoSwype(byte[] fileDigest) {
        //TODO: implement
        if (BuildConfig.DEBUG)
            throw new RuntimeException("Should implement this!");
        return false;
    }

    @Override
    public int getTotalExecutingRequests() {
        int amount = transport.getRequestCount();
        if (orderTask != null && orderTask.isRequestRunningNow())
            ++amount;
        return amount;
    }

    @Override
    public void persistTask(SharedPreferences prefs) {
        OrderTask task = orderTask;
        if (task != null && task.data.type == OrderType.QrCode && !task.isCancelled()) {
            try {
                prefs.edit().putString(ARG_ORDER_REQUEST, orderTask.toJson().toString()).apply();
            } catch (JSONException e) {
                if (BuildConfig.DEBUG)
                    throw new RuntimeException(e);
                else
                    Log.e(TAG, "persistState: ", e);
            }
        }
    }

    @Override
    public boolean resumeTask(SharedPreferences prefs) {
        if (prefs.contains(ARG_ORDER_REQUEST)) {
            try {
                orderTask = new OrderTask(new JSONObject(prefs.getString(ARG_ORDER_REQUEST, null)), new OrderTaskListener(), networkListener, transport);
                if (orderTask.serverUri.equals(serverUri)) {
                    orderTask.resume();
                    return true;
                } else {
                    orderTask = null;
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "restoreState: ", e);
                prefs.edit().remove(ARG_ORDER_REQUEST).apply();
            }
        }
        return false;
    }

    public EnterpriseBalance getBalance() {
        return balanceKeeper.getValue();
    }

    private void getBalance(INetworkRequestListener<EnterpriseBalance> listener) {
        transport.getBalance(serverUri, new NetworkListenerMultiWrapper(new INetworkRequestListener[]{listener, this.networkListener}));
    }

    public FeeEstimate getFeeEstimate() {
        return feeEstimateKeeper.getFee();
    }


    public interface OnGotEstimateFeeListener {
        void onGotFeeEstimate(OrderType orderType, FeeEstimate estimate);
    }

    public interface OnGetFeeErrorListener {
        void onGetFeeError(Exception e);
    }

    public interface OnBalanceUpdateListener {
        void onBalanceUpdate(EnterpriseBalance balance);
    }

    private class NetworkListenerLocal extends NetworkListener implements IGetObjectCallback {

        @Override
        public void onNetworkRequestDone(NetworkRequest request, Object responce) {
            super.onNetworkRequestDone(request, responce);
        }

        @Override
        public void onNetworkRequestCancel(NetworkRequest request) {
            super.onNetworkRequestCancel(request);
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            super.onNetworkRequestError(request, e);
        }

        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            super.onNetworkRequestStart(request);
        }

        @Override
        public void onRequestResult(Object responce) {
        }
    }

    private class OrderTaskListener implements OrderTask.OrderTaskListener {

        @Override
        public void onQrCodeOrderComplete(OrderTask orderTask, IQrCodeOrderResult result) {
            onQrCodeOrderComplete.postNotifyEvent(result, orderTask.getTimeTookToCompleteRequest());
            EnterpriseTransportModel.this.orderTask = null;
            updateBalance(true);
        }

        @Override
        public void onSwypeCodeOrderComplete(OrderTask orderTask, ISwypeCodeOrderResult result) {
            onSwypeCodeOrderComplete.postNotifyEvent(result, orderTask.getTimeTookToCompleteRequest());
            EnterpriseTransportModel.this.orderTask = null;
            updateBalance(true);
        }

        @Override
        public void onPostFileHashOrderComplete(OrderTask orderTask, IPostFileHashOrderResult result) {
            onPostFileOrderComplete.postNotifyEvent(result, orderTask.getTimeTookToCompleteRequest());
            updateBalance(true);
        }

        @Override
        public void onOrderRequestFailed(OrderTask orderTask, Exception e) {
            onOrderRequestFailed.postNotifyEvent(orderTask.data.type, e);
            if (EnterpriseTransportModel.this.orderTask == orderTask)
                EnterpriseTransportModel.this.orderTask = null;
        }

        @Override
        public void onOrderConfirmed(OrderTask orderTask) {
            onOrderConfirmed.postNotifyEvent(orderTask.data.type);
        }
    }
}
