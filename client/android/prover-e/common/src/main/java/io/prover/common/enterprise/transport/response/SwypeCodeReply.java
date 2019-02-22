package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.OrderType;
import io.prover.common.util.JSONObjectHelper;

public class SwypeCodeReply implements IPendingReply, ISwypeCodeOrderResult {
    public final boolean pending;

    public final String swypeId;
    public final String swypeCode;
    public final String swypeSeed;
    public final byte[] transactionHash;

    /*
    {
  "result":
  {
    "swype-id": 5960639,
    "swype-sequence": "*47134376",
    "swype-seed": "98b181ebf3827746a4a22ff18e060c415202b93c434c5dce5113e9e3a3c73c7f"
  }
}
     */

    public SwypeCodeReply(JSONObject source, SwypeCodeReply firstReply) throws JSONException {
        if (source != null && !source.isNull("txhash")) {
            pending = true;
            swypeCode = null;
            swypeId = null;
            swypeSeed = null;
            JSONObjectHelper helper = new JSONObjectHelper(source);
            transactionHash = helper.parseHexAsByteArray("txhash", 32);
        } else if (source == null || source.isNull("swype-sequence")) {
            pending = true;
            swypeCode = null;
            swypeId = null;
            swypeSeed = null;
            transactionHash = null;
        } else {
            pending = false;
            transactionHash = firstReply.transactionHash;
            swypeId = source.getString("swype-id");
            swypeCode = source.getString("swype-sequence");
            swypeSeed = source.getString("swype-seed");
        }
    }

    @Override
    public String getSwypeCode() {
        return swypeCode;
    }

    @Override
    public boolean isFake() {
        return false;
    }

    @Override
    public boolean isStillPending() {
        return pending;
    }

    @Override
    public OrderType getOrderType() {
        return OrderType.SwypeFull;
    }
}
