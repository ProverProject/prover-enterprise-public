package io.prover.clapperboard.enterprise.viewholder;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;

import io.prover.clapperboard.enterprise.R;


/**
 * Created by babay on 24.12.2017.
 */

public class FabHolder {
    private final FloatingActionButton fab;

    private boolean showingClose = false;
    private View.OnClickListener listener;
    private boolean enabled = true;

    public FabHolder(FloatingActionButton fab) {
        this.fab = fab;
        fab.setOnClickListener(v -> {
            if (enabled && listener != null)
                listener.onClick(v);
        });
    }

    public void reset() {
        showingClose = false;
        fab.setImageResource(R.drawable.ic_qr_cross);
    }

    public void setState(boolean showClose, boolean animate) {
        if (showingClose == showClose)
            return;
        showingClose = showClose;
        if (animate) {
            setDrawable(showClose ? R.drawable.qr_to_cross : R.drawable.cross_to_qr);
        } else {
            fab.setImageResource(showClose ? R.drawable.ic_cross : R.drawable.ic_qr_cross);
        }
    }

    private void setDrawable(int drawableId) {
        Drawable dr = AppCompatResources.getDrawable(fab.getContext(), drawableId);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        fab.setImageDrawable(dr);
        if (dr instanceof Animatable)
            ((Animatable) dr).start();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
