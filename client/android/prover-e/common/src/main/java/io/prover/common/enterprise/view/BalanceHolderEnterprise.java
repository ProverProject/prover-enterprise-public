package io.prover.common.enterprise.view;

import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import io.prover.common.enterprise.transport.ProverEnterpriseTransport;
import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.prefs.BalanceHolder;
import io.prover.common.transport.base.INetworkRequestBasicListener;
import io.prover.common.transport.base.NetworkRequest;
import io.prover.common.transport.keeper.IGetObjectCallback;

import static io.prover.common.Const.ENTERPRISE_SERVER_URI;

public class BalanceHolderEnterprise extends BalanceHolder implements IGetObjectCallback<EnterpriseBalance>, INetworkRequestBasicListener<EnterpriseBalance> {

    public BalanceHolderEnterprise(TextView balanceView, ImageView refreshButton) {
        super(balanceView, refreshButton);
    }

    @Override
    public void refresh() {
        String uri = PreferenceManager.getDefaultSharedPreferences(balanceView.getContext())
                .getString(ENTERPRISE_SERVER_URI, "");
        ProverEnterpriseTransport.getInstance().getBalance(Uri.parse(uri), this);
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {
        refresh();
    }

    @Override
    public void onRequestResult(EnterpriseBalance coins) {
        double amountPf = coins.proof.amount.doubleValue() / 1_000_000f;
        double amountXem = coins.xem.amount.doubleValue() / 1_000_000f;

        balanceView.setText(String.format(Locale.getDefault(), "%,.2f PF\n%,.2f XEM", amountPf, amountXem));
        shouldAnimate = false;
        stopRefreshBalanceAnimation();
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, EnterpriseBalance responce) {
        onRequestResult(responce);
    }


}
