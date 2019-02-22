package io.prover.common.enterprise.view;

import android.support.annotation.IntDef;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.Retention;

import io.prover.common.R;
import io.prover.common.enterprise.transport.EnterpriseTransportModel;
import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.enterprise.transport.response.FeeEstimate;
import io.prover.common.transport.OrderType;
import io.prover.common.util.Util;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class FeeEstimateViewHolder {
    private final TextView view;
    private final EnterpriseTransportModel transport;
    @OfferState
    private int offerState;
    private FeeEstimate estimate;
    private EnterpriseBalance balance;
    private Runnable showWalletRunnable;

    public FeeEstimateViewHolder(TextView view, EnterpriseTransportModel transport) {
        this.view = view;

        this.transport = transport;
        view.setOnClickListener(this::onClick);
        transport.onFeeEstimateUpdateListener.add(this::onFeeUpdated);
        transport.onGetFeeErrorListener.add(this::onFeeError);
        transport.onBalanceUpdateListener.add(this::onBalanceUpdate);

        estimate = transport.getFeeEstimate();
        setOfferState(estimate != null ? OfferState.OK : OfferState.NO_DATA);
    }

    private void onBalanceUpdate(EnterpriseBalance balance) {
        this.balance = balance;
        updateView();
    }

    private void onFeeError(Exception e) {
        setOfferState(OfferState.ERROR_GETTING_DATA);
    }

    private void onFeeUpdated(OrderType orderType, FeeEstimate estimate) {
        this.estimate = estimate;
        updateView();
    }

    public void setVisible(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setShowWalletRunnable(Runnable showWalletRunnable) {
        this.showWalletRunnable = showWalletRunnable;
    }

    private void onClick(View v) {
        switch (offerState) {
            case OfferState.NO_DATA:
            case OfferState.ERROR_GETTING_DATA:
            case OfferState.OK:
            case OfferState.OUTDATED:
                transport.updateFeeEstimate(true);
                break;

            case OfferState.NOT_ENOUGH_MONEY:
                if (showWalletRunnable != null) {
                    showWalletRunnable.run();
                }
                break;
        }
    }


    private void updateView() {
        if (estimate == null) {
            setOfferState(OfferState.NO_DATA);
        } else if (estimate.isOutdated()) {
            setOfferState(OfferState.OUTDATED);
        } else if (balance == null || estimate.proof.compareTo(balance.proof) <= 0
                || estimate.xem.compareTo(balance.xem) <= 0) {
            setOfferState(OfferState.OK);
        } else {
            setOfferState(OfferState.NOT_ENOUGH_MONEY);
        }
    }

    private void setOfferState(@OfferState int offerState) {
        this.offerState = offerState;
        switch (offerState) {
            case OfferState.NO_DATA:
                transport.updateFeeEstimate(false);
                transport.updateBalance(false);
                view.setText(R.string.requesting_price);
                view.setBackgroundResource(R.drawable.offer_background);
                break;

            case OfferState.OK:
                String balancePf = Util.toDecimalString(estimate.proof.amount, 6, 0, ",", ".");
                String balanceXEM = Util.toDecimalString(estimate.xem.amount, 6, 0, ",", ".");
                String price = view.getResources().getString(R.string.price, balancePf);
                price = price + "\n" + view.getResources().getString(R.string.xem, balanceXEM);
                view.setText(price);
                view.setBackgroundResource(R.drawable.offer_background);
                break;

            case OfferState.ERROR_GETTING_DATA:
                view.setText(R.string.cantGetPrice);
                view.setBackgroundResource(R.drawable.offer_background_error);
                break;

            case OfferState.NOT_ENOUGH_MONEY:
                view.setText(R.string.notEnoughMoney);
                view.setBackgroundResource(R.drawable.offer_background_error);
                break;

            case OfferState.OUTDATED:
                view.setText(R.string.offers_outdated);
                view.setBackgroundResource(R.drawable.offer_background_error);
                break;
        }
    }

    @IntDef({
            OfferState.NO_DATA,
            OfferState.OK,
            OfferState.ERROR_GETTING_DATA,
            OfferState.OUTDATED,
            OfferState.NOT_ENOUGH_MONEY
    })
    @Retention(SOURCE)
    public @interface OfferState {
        int NO_DATA = 0;
        int OK = 1;
        int ERROR_GETTING_DATA = 2;
        int OUTDATED = 3;
        int NOT_ENOUGH_MONEY = 4;
    }
}
