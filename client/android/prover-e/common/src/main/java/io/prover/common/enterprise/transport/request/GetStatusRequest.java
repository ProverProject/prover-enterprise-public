package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.response.ServerStatus;
import io.prover.common.transport.base.INetworkRequestListener;
import okhttp3.OkHttpClient;

public class GetStatusRequest extends ProverEnterpriseRequest<ServerStatus> {

    public GetStatusRequest(OkHttpClient client, Uri server, INetworkRequestListener<ServerStatus> listener) {
        super(client, server, "get-status", listener);
    }

    @Override
    protected ServerStatus parse(String source, int status) throws JSONException {
        return new ServerStatus(new JSONObject(source).getJSONObject("result"));
    }
}
