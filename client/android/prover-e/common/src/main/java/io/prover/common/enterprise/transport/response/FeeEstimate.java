package io.prover.common.enterprise.transport.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeeEstimate {
    public static final long OUTDATE_TIME = 300_000;

    public final List<NetCoins> balances;
    public final NetCoins proof;
    public final NetCoins xem;

    public FeeEstimate(JSONObject src) throws JSONException {
        JSONArray srcBalance = src.getJSONArray("result");
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

    public FeeEstimate(NetCoins proof, NetCoins xem) {
        this.proof = proof;
        this.xem = xem;
        balances = Collections.unmodifiableList(Arrays.asList(proof, xem));
    }

    public FeeEstimate(List<NetCoins> balances) {

        NetCoins proofBalance = null;
        NetCoins xemBalance = null;

        for (NetCoins coins : balances) {
            if ("prover".equals(coins.namespaceId))
                proofBalance = coins;
            else if ("nem".equals(coins.namespaceId))
                xemBalance = coins;
        }
        this.balances = Collections.unmodifiableList(balances);
        this.proof = proofBalance;
        this.xem = xemBalance;
    }

    public FeeEstimate add(FeeEstimate other) {
        if (balances.size() == 2) {
            NetCoins proof = this.proof.add(other.proof);
            NetCoins xem = this.xem.add(other.xem);
            return new FeeEstimate(proof, xem);
        }

        List<NetCoins> res = new ArrayList<>();
        for (NetCoins coins : balances) {
            for (NetCoins otherCoins : other.balances) {
                if (otherCoins.namespaceId.equals(coins.namespaceId)) {
                    res.add(coins.add(otherCoins));
                    break;
                }
            }
        }

        return new FeeEstimate(res);
    }

    public boolean isOutdated() {
        return false;
    }
}
