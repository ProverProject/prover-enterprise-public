package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.transport.IPostFileHashOrderResult;
import io.prover.common.transport.OrderType;

public class SubmitMediaHashReply implements IPendingReply, IPostFileHashOrderResult {

    public final String txHash;
    public final int confirmations;
    public final Integer height;
    protected final boolean isPending;

    /*
    "confirmations": 0,
    "height": null
     */

    public SubmitMediaHashReply(JSONObject source, SubmitMediaHashReply firstReply) throws JSONException {
        if (firstReply == null) {
            txHash = source.getString("txhash");
            confirmations = 0;
            height = null;
            isPending = true;
        } else {
            txHash = firstReply.txHash;
            confirmations = source.getInt("confirmations");
            if (source.isNull("height")) {
                height = null;
                isPending = true;
            } else {
                this.height = source.getInt("height");
                isPending = false;
            }
        }
    }

    @Override
    public boolean isStillPending() {
        return isPending;
    }

    @Override
    public OrderType getOrderType() {
        return OrderType.FileHash;
    }
}
