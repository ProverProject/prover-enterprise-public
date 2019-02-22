package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.prover.common.enterprise.transport.response.QrCodeReply;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.util.encoders.Hex;
import okhttp3.OkHttpClient;

public class SubmitMessageRequest extends ProverEnterpriseRequest<QrCodeReply> {

    private final QrCodeReply firstReply;
    private final String message;

    /**
     * call to put message to blockchain to get QrCode.
     *
     * @param client
     * @param message
     * @param clientId
     * @param listener
     */
    public SubmitMessageRequest(OkHttpClient client, Uri server, String message, long clientId, INetworkRequestListener<QrCodeReply> listener) {
        super(client, server, "submit-message", listener);
        this.firstReply = null;
        this.message = message;
        parameters.add("message", message);
        parameters.add("clientid", Long.toString(clientId));
    }

    /**
     * call to check whether request is complete
     *
     * @param client
     * @param firstReply
     * @param listener
     */
    public SubmitMessageRequest(OkHttpClient client, Uri server, QrCodeReply firstReply, INetworkRequestListener<QrCodeReply> listener) {
        super(client, server, "submit-message", listener);
        this.firstReply = firstReply;
        this.message = firstReply.message;
        parameters.add("txhash", Hex.toHexString(firstReply.txHash));
    }

    @Override
    protected QrCodeReply parse(String source, int status) throws IOException, JSONException {
        return new QrCodeReply(new JSONObject(source).getJSONObject("result"), firstReply, message);
    }
}
