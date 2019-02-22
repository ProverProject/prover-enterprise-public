package io.prover.common.enterprise.transport.request;

import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.response.SwypeCodeReply;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.util.encoders.Hex;
import okhttp3.OkHttpClient;

public class RequestSwypeCodeRequest extends ProverEnterpriseRequest<SwypeCodeReply> {

    private final SwypeCodeReply firstReply;

    public RequestSwypeCodeRequest(OkHttpClient client, Uri server, @Nullable SwypeCodeReply firstReply, INetworkRequestListener<SwypeCodeReply> listener) {
        super(client, server, "request-swype-code", listener);

        if (firstReply != null)
            parameters.add("txhash", Hex.toHexString(firstReply.transactionHash));
        this.firstReply = firstReply;
    }

    @Override
    protected SwypeCodeReply parse(String source, int status) throws JSONException {

        return new SwypeCodeReply(new JSONObject(source).optJSONObject("result"), firstReply);
    }
}