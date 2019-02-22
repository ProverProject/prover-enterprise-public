package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.MediaHashRequestData;
import io.prover.common.enterprise.transport.response.SubmitMediaHashReply;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.util.encoders.Hex;
import okhttp3.OkHttpClient;

public class SubmitMediaHashRequest extends ProverEnterpriseRequest<SubmitMediaHashReply> {

    private final SubmitMediaHashReply firstReply;

    public SubmitMediaHashRequest(OkHttpClient client, Uri server, MediaHashRequestData request, INetworkRequestListener<SubmitMediaHashReply> listener) {
        super(client, server, "submit-media-hash", listener);
        firstReply = null;

        parameters.add("mediahash", Hex.toHexString(request.digest));
        parameters.add("mediahashtype", request.hashType.toServerString());
        parameters.add("clientid", Long.toString(request.cliendId));

        if (request.swypeCodeReply != null) {
            parameters.add("referencetxhash", Hex.toHexString(request.swypeCodeReply.transactionHash));
        } else {
            parameters.add("referenceblockheight", request.swypeCodeFastReply.referenceBlockHeight.toString());
        }
    }

    public SubmitMediaHashRequest(OkHttpClient client, Uri server, SubmitMediaHashReply firstReply, INetworkRequestListener<SubmitMediaHashReply> listener) {
        super(client, server, "submit-media-hash", listener);
        parameters.add("txhash", firstReply.txHash);
        this.firstReply = firstReply;
    }

    @Override
    protected SubmitMediaHashReply parse(String source, int status) throws JSONException {
        return new SubmitMediaHashReply(new JSONObject(source).getJSONObject("result"), firstReply);
    }
}
