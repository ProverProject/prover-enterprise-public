package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.response.SwypeCodeFastReply;
import io.prover.common.transport.base.INetworkRequestListener;
import okhttp3.OkHttpClient;

public class SwypeCodeFastRequest extends ProverEnterpriseRequest<SwypeCodeFastReply> {
    public SwypeCodeFastRequest(OkHttpClient client, Uri server, INetworkRequestListener<SwypeCodeFastReply> listener) {
        super(client, server, "fast-request-swype-code", listener);
    }

    @Override
    protected SwypeCodeFastReply parse(String source, int status) throws JSONException {
        return new SwypeCodeFastReply(new JSONObject(source).getJSONObject("result"));
    }
}
