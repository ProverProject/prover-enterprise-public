package io.prover.common.enterprise.transport.request;

import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.MediaHashRequestData;
import io.prover.common.enterprise.transport.OrderRequestData;
import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.base.INetworkRequestListener;
import okhttp3.OkHttpClient;

public class EstimateFeeRequest extends ProverEnterpriseRequest<FeeEstimate> {

    private final OrderType orderType;
    private final OrderRequestData requestData;

    public EstimateFeeRequest(OkHttpClient client, Uri server, OrderType orderType, @Nullable OrderRequestData requestData, INetworkRequestListener<FeeEstimate> listener) {
        super(client, server, "estimate-fee", listener);

        switch (orderType) {

            case QrCode:
                if (requestData == null)
                    throw new NullPointerException("requestData should be not null for QrCode request");
                parameters.add("request", "submit-message")
                        .add("message", requestData.message)
                        .add("clientid", "0");
                break;

            case SwypeFull:
                parameters.add("request", "request-swype-code");
                break;

            case SwypeFast:
                //estimate fee for posting media hash with swype-code-fast
                parameters.add("request", "submit-media-hash")
                        .add("mediahash", "0000000000000000000000000000000000000000000000000000000000000000")
                        .add("mediahashtype", MediaHashRequestData.MediHashType.SHA256.toServerString())
                        .add("clientid", "0")
                        .add("referenceblockheight", "0");
                break;

            case FileHash:
                parameters.add("request", "submit-media-hash")
                        .add("mediahash", "0000000000000000000000000000000000000000000000000000000000000000")
                        .add("mediahashtype", MediaHashRequestData.MediHashType.SHA256.toServerString())
                        .add("clientid", "0")
                        .add("referencetxhash", "0000000000000000000000000000000000000000000000000000000000000000");
                break;
        }

        this.orderType = orderType;
        this.requestData = requestData;
    }

    @Override
    protected FeeEstimate parse(String source, int status) throws JSONException {
        return new FeeEstimate(new JSONObject(source));
    }
}
