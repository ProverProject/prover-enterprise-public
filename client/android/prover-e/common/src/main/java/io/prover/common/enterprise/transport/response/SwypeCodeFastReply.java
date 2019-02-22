package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.OrderType;

public class SwypeCodeFastReply implements IPendingReply, ISwypeCodeOrderResult {

    public Integer referenceBlockHeight;
    public String swypeId;
    public String swypeCode;
    public String swypeSeed;


    public SwypeCodeFastReply(JSONObject source) throws JSONException {
        this.referenceBlockHeight = source.isNull("reference-block-height") ? null :
                source.getInt("reference-block-height");
        this.swypeId = source.getString("swype-id");
        this.swypeCode = source.getString("swype-sequence");
        this.swypeSeed = source.getString("swype-seed");
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
        return false;
    }

    @Override
    public OrderType getOrderType() {
        return OrderType.SwypeFast;
    }
}
