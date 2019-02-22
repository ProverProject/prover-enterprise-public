package io.prover.common.transport.base;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;

public abstract class FormRequest<Result> extends NetworkRequest<Result> {

    protected final FormBody.Builder parameters = new FormBody.Builder();
    private final String method;
    private final RequestType requestType;

    public FormRequest(OkHttpClient client, String method, RequestType requestType, INetworkRequestListener<Result> listener) {
        super(client, listener);
        this.method = method;
        this.requestType = requestType;
    }

    public FormRequest(OkHttpClient client, String method, INetworkRequestListener<Result> listener) {
        this(client, method, RequestType.Post, listener);
    }

    public FormRequest(OkHttpClient client, String siteUrl, String method, RequestType requestType, INetworkRequestListener<Result> listener) {
        super(client, siteUrl, listener);
        this.method = method;
        this.requestType = requestType;
    }

    public FormRequest(OkHttpClient client, String siteUrl, String method, INetworkRequestListener<Result> listener) {
        this(client, siteUrl, method, RequestType.Post, listener);
    }

    @Override
    protected void run() {
        if (requestType.isEnclosing())
            execSimpleRequest(method, requestType, parameters.build());
        else
            execSimpleRequest(method, requestType, (String) null);
    }

    public String getMethod() {
        return method;
    }
}
