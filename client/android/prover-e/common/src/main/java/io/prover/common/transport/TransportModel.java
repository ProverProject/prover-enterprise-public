package io.prover.common.transport;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList2;
import io.prover.common.transport.base.INetworkRequestBasicListener;
import io.prover.common.transport.base.INetworkRequestCancelListener;
import io.prover.common.transport.base.INetworkRequestErrorListener;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.INetworkRequestStartListener;
import io.prover.common.transport.base.NetworkRequest;

public abstract class TransportModel {

    protected final Handler handler = new Handler(Looper.getMainLooper());
    public final ListenerList2<RunningRequestsChangedListener, Integer, Boolean> onRunningRequestsAmountChanged
            = new ListenerList2<>(handler, RunningRequestsChangedListener::onRunningRequestsAmountChanged);
    public final RequestAmountChangesHandler requestAmountHandler = new RequestAmountChangesHandler(this::onRunningRequestsAmountChanged);
    public final ListenerList1<OrderConfirmedListener, OrderType> onOrderConfirmed
            = new ListenerList1<>(handler, OrderConfirmedListener::onOrderConfirmed);

    public final ListenerList2<SwypeCodeOrderCompleteListener, ISwypeCodeOrderResult, Long> onSwypeCodeOrderComplete
            = new ListenerList2<>(handler, SwypeCodeOrderCompleteListener::onSwypeCodeOrderComplete);

    public final ListenerList2<QrCodeOrderCompleteListener, IQrCodeOrderResult, Long> onQrCodeOrderComplete
            = new ListenerList2<>(handler, QrCodeOrderCompleteListener::onQrCodeOrderComplete);

    public final ListenerList2<PostFileHashOrderCompleteListener, IPostFileHashOrderResult, Long> onPostFileOrderComplete
            = new ListenerList2<>(handler, PostFileHashOrderCompleteListener::onFileHashOrderComplete);

    public final ListenerList2<OrderRequestFailedListener, OrderType, Exception> onOrderRequestFailed
            = new ListenerList2<>(handler, OrderRequestFailedListener::onOrderRequestFailed);

    public final ListenerList2<INetworkRequestBasicListener, NetworkRequest, Object> onNetworkRequestDoneListener
            = new ListenerList2<>(handler, INetworkRequestBasicListener::onNetworkRequestDone);

    public final ListenerList2<INetworkRequestErrorListener, NetworkRequest, Exception> onNetworkRequestErrorListener
            = new ListenerList2<>(handler, INetworkRequestErrorListener::onNetworkRequestError);

    public final ListenerList1<INetworkRequestStartListener, NetworkRequest> onNetworkRequestStartListener
            = new ListenerList1<>(handler, INetworkRequestStartListener::onNetworkRequestStart);

    public final ListenerList1<INetworkRequestCancelListener, NetworkRequest> onNetworkRequestCancelListener
            = new ListenerList1<>(handler, INetworkRequestCancelListener::onNetworkRequestCancel);

    /**
     * call when transport should prepare (or not) to do transaction requests
     *
     * @param ready
     */
    public abstract void setBeReadyToWork(boolean ready);

    /**
     * call when transport should be ready to start main task (refresh offers) ot stop doing that
     * if ready == true - try to request offers
     *
     * @param ready
     */
    public abstract void setBeReadyToStart(boolean ready);

    public abstract boolean isReadyForMainTask();

    public abstract void onActivityResume();

    public abstract void onActivityPause();

    /**
     * @param message
     * @return true if request sent
     */
    public abstract boolean requestQrCode(@NonNull String message);

    /**
     * @return true if request sent
     */
    public abstract boolean requestSwypeCode();

    /**
     * post file hash with swype code
     *
     * @param swypeOrder
     * @param digest
     * @return true if request sent
     */
    public abstract boolean postFileHash(@NonNull ISwypeCodeOrderResult swypeOrder, @NonNull byte[] digest);

    /**
     * post file hash without swype code.
     *
     * @param fileDigest
     */
    public abstract boolean postFileHashNoSwype(byte[] fileDigest);

    public abstract int getTotalExecutingRequests();

    public abstract void persistTask(SharedPreferences prefs);

    public abstract boolean resumeTask(SharedPreferences prefs);

    private void onRunningRequestsAmountChanged(int amount, boolean latestRequestWasWithError) {
        amount = Math.max(amount, getTotalExecutingRequests());
        onRunningRequestsAmountChanged.postNotifyEvent(amount, latestRequestWasWithError);
    }

    @MainThread
    public interface OrderConfirmedListener {
        void onOrderConfirmed(@NonNull OrderType orderType);
    }

    @MainThread
    public interface SwypeCodeOrderCompleteListener {
        void onSwypeCodeOrderComplete(ISwypeCodeOrderResult result, long timeSpent);
    }

    @MainThread
    public interface QrCodeOrderCompleteListener {
        void onQrCodeOrderComplete(IQrCodeOrderResult result, long timeSpent);
    }

    @MainThread
    public interface PostFileHashOrderCompleteListener {
        void onFileHashOrderComplete(IPostFileHashOrderResult result, long timeSpent);
    }

    @MainThread
    public interface OrderRequestFailedListener {
        void onOrderRequestFailed(@NonNull OrderType orderType, Exception e);
    }

    public interface RunningRequestsChangedListener {
        void onRunningRequestsAmountChanged(int amount, boolean latestRequestWasWithError);
    }

    public class NetworkListener implements INetworkRequestListener {
        @Override
        public void onNetworkRequestDone(NetworkRequest request, Object responce) {
            onNetworkRequestDoneListener.postNotifyEvent(request, responce);
            requestAmountHandler.onRequestAmountChanged(getTotalExecutingRequests());
        }

        @Override
        public void onNetworkRequestCancel(NetworkRequest request) {
            onNetworkRequestCancelListener.postNotifyEvent(request);
            requestAmountHandler.onRequestAmountChanged(getTotalExecutingRequests());
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            onNetworkRequestErrorListener.postNotifyEvent(request, e);
            requestAmountHandler.onNetworkRequestGotError();
            requestAmountHandler.onRequestAmountChanged(getTotalExecutingRequests());
        }

        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            onNetworkRequestStartListener.postNotifyEvent(request);
            requestAmountHandler.onRequestAmountChanged(getTotalExecutingRequests());
        }
    }

}
