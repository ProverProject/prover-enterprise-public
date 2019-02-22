package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.transport.base.INetworkRequestListener;
import okhttp3.OkHttpClient;

public class GetBalanceRequest extends ProverEnterpriseRequest<EnterpriseBalance> {
    public GetBalanceRequest(OkHttpClient client, Uri server, INetworkRequestListener<EnterpriseBalance> listener) {
        super(client, server, "get-balance", listener);
    }

    @Override
    protected EnterpriseBalance parse(String source, int status) throws JSONException {
        return new EnterpriseBalance(new JSONObject(source).getJSONObject("result"));
    }
}
