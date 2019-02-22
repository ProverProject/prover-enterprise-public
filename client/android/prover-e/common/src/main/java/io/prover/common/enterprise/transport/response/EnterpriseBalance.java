package io.prover.common.enterprise.transport.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnterpriseBalance {
    public final List<NetCoins> balances;
    public final String address;
    public final NetCoins proof;
    public final NetCoins xem;

    public EnterpriseBalance(JSONObject src) throws JSONException {
        address = src.getString("address");
        JSONArray srcBalance = src.getJSONArray("balance");
        ArrayList<NetCoins> balances = new ArrayList<>();
        NetCoins proofBalance = null;
        NetCoins xemBalance = null;

        for (int i = 0; i < srcBalance.length(); ++i) {
            NetCoins coins = new NetCoins(srcBalance.getJSONObject(i));
            balances.add(coins);
            if ("prover".equals(coins.namespaceId))
                proofBalance = coins;
            else if ("nem".equals(coins.namespaceId))
                xemBalance = coins;
        }
        this.balances = Collections.unmodifiableList(balances);
        this.proof = proofBalance;
        this.xem = xemBalance;
    }
}
