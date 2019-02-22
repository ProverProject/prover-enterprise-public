package io.prover.common.transport.base;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;

public class Transport {

    protected final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    protected final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicInteger requestCounter = new AtomicInteger();

    public int getRequestCount() {
        return requestCounter.get();
    }

    protected Session getCheckSession(@Nullable INetworkRequestBasicListener listener) {
        Session session = SessionManager.INSTANCE.getSession();
        if (session == null || session.expired()) {
            Exception e = new Exception("No session");
            if (listener instanceof INetworkRequestErrorListener)
                handler.post(() -> ((INetworkRequestErrorListener) listener).onNetworkRequestError(null, e));
            return null;
        }
        return session;
    }

    /**
     * wraps normal requests
     */
    protected class NetworkRequestWrapper<T> implements INetworkRequestListener<T> {
        @Nullable
        private final INetworkRequestBasicListener<T> callback;

        public NetworkRequestWrapper(@Nullable INetworkRequestBasicListener<T> callback) {
            this.callback = callback;
        }

        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            requestCounter.incrementAndGet();
            if (callback != null) {
                if (callback instanceof INetworkRequestStartListener)
                    handler.post(() -> ((INetworkRequestStartListener) callback).onNetworkRequestStart(request));
            }
        }

        @Override
        public void onNetworkRequestDone(NetworkRequest request, T responce) {
            requestCounter.decrementAndGet();
            if (callback != null)
                handler.post(() -> callback.onNetworkRequestDone(request, responce));
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            requestCounter.decrementAndGet();
            if (callback instanceof INetworkRequestErrorListener)
                handler.post(() -> ((INetworkRequestErrorListener) callback).onNetworkRequestError(request, e));
        }

        @Override
        public void onNetworkRequestCancel(NetworkRequest request) {
            requestCounter.decrementAndGet();
            if (callback instanceof INetworkRequestCancelListener) {
                handler.post(() -> ((INetworkRequestCancelListener) callback).onNetworkRequestCancel(request));
            }
        }
    }

    protected class SessionRequestWrapper extends NetworkRequestWrapper<Session> {
        public SessionRequestWrapper(@Nullable INetworkRequestBasicListener<Session> callback) {
            super(callback);
        }

        @Override
        public void onNetworkRequestDone(NetworkRequest request, Session responce) {
            SessionManager.INSTANCE.setSession(responce);
            super.onNetworkRequestDone(request, responce);
        }
    }
}