package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class NetCoins {

    /*
      {
        "mosaicId":
        {
          "namespaceId": "prover",
          "name": "proof"
        },
        "quantity": 17550000000
      }
     */

    public final BigInteger amount;
    public final String namespaceId;
    public final String name;

    public NetCoins(JSONObject src) throws JSONException {
        String amountStr = src.getString("quantity");
        amount = new BigInteger(amountStr);
        JSONObject mosaicId = src.getJSONObject("mosaicId");
        namespaceId = mosaicId.getString("namespaceId");
        name = mosaicId.getString("name");
    }

    public NetCoins(BigInteger amount, String namespaceId, String name) {
        this.amount = amount;
        this.namespaceId = namespaceId;
        this.name = name;
    }

    public NetCoins add(NetCoins other) {
        return new NetCoins(amount.add(other.amount), namespaceId, name);
    }

    public int compareTo(NetCoins other) {
        return amount.compareTo(other.amount);
    }
}
