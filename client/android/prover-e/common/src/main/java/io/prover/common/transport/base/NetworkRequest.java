package io.prover.common.transport.base;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import io.prover.common.BuildConfig;
import io.prover.common.Const;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by babay on 14.11.2017.
 */

public abstract class NetworkRequest<Result> {
    public static final String TAG = Const.TAG + "NetRequest";
    protected final INetworkRequestListener<Result> listener;
    private final OkHttpClient client;
    private final String siteUrl;
    protected RequestLog debugData;
    protected String charset = "UTF-8";
    protected volatile boolean cancelled;
    protected volatile boolean finished;
    private int status;

    public NetworkRequest(OkHttpClient client, INetworkRequestListener<Result> listener) {
        this.client = client;
        this.listener = listener;
        siteUrl = Const.SITE_URL;
    }

    public NetworkRequest(OkHttpClient client, String siteUrl, INetworkRequestListener<Result> listener) {
        this.listener = listener;
        this.client = client;
        this.siteUrl = siteUrl;
    }

    public NetworkRequest execute() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            listener.onNetworkRequestStart(this);
            run();
        });
        return this;
    }

    protected Request createRequest(String method, RequestType requestType, String requestBody) throws IOException {
        RequestBody body = requestBody == null ? null : RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestBody);
        return createRequest(method, requestType, body);
    }

    protected Request createRequest(String method, RequestType requestType, RequestBody requestBody) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(siteUrl + method);
        if (requestType.isEnclosing() && requestBody != null) {
            builder.method(requestType.requestTypeString(), requestBody);
        } else {
            builder.method(requestType.requestTypeString(), null);
        }
        //builder.addHeader("Accept", "application/json");

        Request request = builder.build();

        if (BuildConfig.DEBUG) {
            debugData = new RequestLog(requestType);
            debugData.onStart(request);
        }

        return request;
    }

    private String execRequest(Request request) throws IOException {
        if (debugData != null) {
            Log.d(TAG, "sending " + debugData.toString());
        }
        try (Response response = client.newCall(request).execute()) {
            status = response.code();
            String result = response.body().string();
            if (debugData != null)
                debugData.onGotResponse(result, response.code());
            return result;
        }
    }

    private void handleResponce(String responceStr) throws IOException, JSONException {
        if (!isResponseOk(responceStr, status)) {
            handleException(parseException(responceStr, status));
            return;
        }

        Result responce = parse(responceStr, status);

        if (debugData != null)
            debugData.log();

        if (finish() && listener != null) {
            listener.onNetworkRequestDone(this, responce);
        }
    }

    protected void handleException(Exception ex) {
        if (debugData != null) {
            debugData.setException(ex).log();
        }
        if (finish() && listener != null) {
            listener.onNetworkRequestError(this, ex);
        }
    }

    protected void execSimpleRequest(String method, RequestType requestType, @Nullable String requestBody) {
        try {
            Request request = createRequest(method, requestType, requestBody);
            String responce = execRequest(request);
            handleResponce(responce);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    protected void execSimpleRequest(String method, RequestType requestType, @Nullable RequestBody requestBody) {
        try {
            Request request = createRequest(method, requestType, requestBody);
            String response = execRequest(request);
            handleResponce(response);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    public void cancel() {
        cancelled = true;
        if (finish() && listener != null)
            listener.onNetworkRequestCancel(this);
    }


    /**
     * finishes request
     *
     * @return true if request was not finished
     */
    protected boolean finish() {
        boolean wasFinished;
        synchronized (this) {
            wasFinished = finished;
            finished = true;
        }
        return !wasFinished;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    protected abstract Result parse(String source, int status) throws IOException, JSONException;

    protected abstract Exception parseException(String responceStr, int code);

    protected boolean isResponseOk(String response, int code) {
        return code == 200;
    }

    protected abstract void run();


}