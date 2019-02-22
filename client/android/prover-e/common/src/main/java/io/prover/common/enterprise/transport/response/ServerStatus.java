package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import io.prover.common.util.JSONObjectHelper;

public class ServerStatus {

    public final String version;

    public final BigInteger networkTopBlock;
    public final BigInteger indexTopBlock;
    public final BigInteger targetTopBlock;

    public final String state;
    public final String address;

    public ServerStatus(JSONObject src) throws JSONException {
        version = src.getString("version");

        JSONObjectHelper helper = new JSONObjectHelper(src.getJSONObject("index"));
        networkTopBlock = helper.getUnsignedBigInteger("networkTopBlock", false);
        indexTopBlock = helper.getUnsignedBigInteger("indexTopBlock", false);
        targetTopBlock = helper.getUnsignedBigInteger("targetTopBlock", false);

        JSONObject srcKeystore = src.getJSONObject("keystore");
        state = srcKeystore.getString("state");
        address = srcKeystore.getString("address");
    }
}