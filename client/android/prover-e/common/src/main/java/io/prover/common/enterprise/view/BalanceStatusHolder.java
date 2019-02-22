package io.prover.common.enterprise.view;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.math.BigInteger;
import java.util.Locale;

import io.prover.common.R;
import io.prover.common.enterprise.transport.EnterpriseTransportModel;
import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.transport.TransportModel;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by babay on 22.11.2017.
 */

public class BalanceStatusHolder implements View.OnClickListener, TransportModel.RunningRequestsChangedListener {
    private final ViewGroup root;
    private final EnterpriseTransportModel transport;
    private final TextView balanceView;
    private final ImageView proverWalletStatusIcon;
    private final View openWalletView;
    private VectorDrawableCompat okDrawable;
    private Drawable progressDrawable;

    @State
    private int state = State.ONLINE;

    public BalanceStatusHolder(ViewGroup root, EnterpriseTransportModel transport) {
        this.root = root.findViewById(R.id.balanceContainer);
        this.transport = transport;
        balanceView = root.findViewById(R.id.balanceView);
        proverWalletStatusIcon = root.findViewById(R.id.proverWalletStatusIcon);
        openWalletView = root.findViewById(R.id.openWalletIcon);

        transport.onBalanceUpdateListener.add(this::setCoins);
        transport.onRunningRequestsAmountChanged.add(this);
        this.root.setOnClickListener(this);
        EnterpriseBalance coins = transport.getBalance();
        if (coins != null) {
            setCoins(coins);
        } //else {
        transport.updateBalance(true);
        //}
    }

    public void setState(@State int state) {
        if (this.state == state)
            return;
        this.state = state;

        switch (state) {
            case State.OFFLINE:
                Drawable dr = AppCompatResources.getDrawable(root.getContext(), R.drawable.ic_prover_offline);
                dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                proverWalletStatusIcon.setImageDrawable(dr);
                if (balanceView.getText().length() == 0)
                    balanceView.setText(R.string.offline);
                balanceView.setCompoundDrawables(null, null, null, null);
                break;

            case State.ONLINE:
                if (okDrawable == null) {
                    okDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_ok, null);
                    okDrawable.setBounds(0, 0, okDrawable.getIntrinsicWidth(), okDrawable.getIntrinsicHeight());
                }
                proverWalletStatusIcon.setImageDrawable(okDrawable);
                if (progressDrawable instanceof Animatable)
                    ((Animatable) progressDrawable).stop();
                break;

            case State.EXECUTING:
                if (progressDrawable == null) {
                    progressDrawable = AppCompatResources.getDrawable(root.getContext(), R.drawable.ic_prover_connecting_animated);
                    progressDrawable.setBounds(0, 0, progressDrawable.getIntrinsicWidth(), progressDrawable.getIntrinsicHeight());
                }
                proverWalletStatusIcon.setImageDrawable(progressDrawable);
                if (progressDrawable instanceof Animatable)
                    ((Animatable) progressDrawable).start();
                break;
        }
    }

    private void setCoins(EnterpriseBalance coins) {
        double amountPf = coins.proof.amount.doubleValue() / 1_000_000f;
        double amountXem = coins.xem.amount.doubleValue() / 1_000_000f;

        balanceView.setText(String.format(Locale.getDefault(), "%,.2f PF\n%,.2f XEM", amountPf, amountXem));
        int color = (coins.proof.amount.equals(BigInteger.ZERO) || coins.xem.amount.equals(BigInteger.ZERO))
                ? balanceView.getResources().getColor(R.color.colorAccent) : 0xFFFFFFFF;
        balanceView.setTextColor(color);
    }

    @Override
    public void onClick(View v) {
        transport.updateBalance(true);
    }

    public void setOnOpenWalletClickLictener(View.OnClickListener listener) {
        root.setOnClickListener(listener);
    }

    @Override
    public void onRunningRequestsAmountChanged(int amount, boolean latestRequestWasWithError) {
        if (amount > 0)
            setState(State.EXECUTING);
        else
            setState(latestRequestWasWithError ? State.OFFLINE : State.ONLINE);
    }

    @IntDef({
            State.ONLINE,
            State.OFFLINE,
            State.EXECUTING
    })
    @Retention(SOURCE)
    protected @interface State {
        /**
         * We're online and all is OK
         */
        int ONLINE = 0;
        /**
         * no connection -- some error, can't even get balance
         */
        int OFFLINE = 1;
        /**
         * showing executing animation
         */
        int EXECUTING = 2;

    }
}
