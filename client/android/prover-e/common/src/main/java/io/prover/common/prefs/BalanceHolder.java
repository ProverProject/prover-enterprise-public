package io.prover.common.prefs;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.content.res.AppCompatResources;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.prover.common.R;
import io.prover.common.transport.base.INetworkRequestCancelListener;
import io.prover.common.transport.base.INetworkRequestErrorListener;
import io.prover.common.transport.base.INetworkRequestStartListener;
import io.prover.common.transport.base.NetworkRequest;

public abstract class BalanceHolder implements INetworkRequestErrorListener, INetworkRequestCancelListener, INetworkRequestStartListener {
    protected final ImageView refreshButton;
    protected final TextView balanceView;
    protected final Handler handler = new Handler();
    protected boolean shouldAnimate;
    protected long animationStartTime = 0;

    public BalanceHolder(TextView balanceView, ImageView refreshButton) {
        this.refreshButton = refreshButton;
        this.balanceView = balanceView;
        refreshButton.setOnClickListener(v -> refresh());
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        if (!shouldAnimate) {
            animationStartTime = System.currentTimeMillis();
            shouldAnimate = true;
            Drawable dr = AppCompatResources.getDrawable(balanceView.getContext(), R.drawable.ic_refresh_anim);
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            if (dr instanceof Animatable)
                ((Animatable) dr).start();
            refreshButton.setImageDrawable(dr);
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        shouldAnimate = false;
        stopRefreshBalanceAnimation();
        Toast.makeText(balanceView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        if (balanceView.getText().length() == 0) {
            balanceView.setText(R.string.offline);
        }
    }

    @Override
    public void onNetworkRequestCancel(NetworkRequest request) {
        shouldAnimate = false;
        stopRefreshBalanceAnimation();
    }

    protected void stopRefreshBalanceAnimation() {
        if (shouldAnimate)
            return;
        if (System.currentTimeMillis() - animationStartTime < 1000) {
            handler.postDelayed(this::stopRefreshBalanceAnimation, 1000 - (System.currentTimeMillis() - animationStartTime));
        } else {
            refreshButton.setImageResource(R.drawable.ic_refresh);
        }
    }

    public abstract void refresh();

    public abstract void onActivityPause();

    public abstract void onActivityResume();
}
