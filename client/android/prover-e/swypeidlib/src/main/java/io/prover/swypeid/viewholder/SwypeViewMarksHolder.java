package io.prover.swypeid.viewholder;

import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import io.prover.swypeidlib.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SwypeViewMarksHolder {

    private final LinearLayout root;
    private int currentMark = 0;

    public SwypeViewMarksHolder(LinearLayout root) {
        this.root = root;
    }

    public void setMarksAmount(int amount) {
        if (root.getChildCount() != amount) {
            root.removeAllViews();
            Resources res = root.getResources();
            int dp8 = (int) (res.getDisplayMetrics().density * 8);
            int dp6 = (int) (res.getDisplayMetrics().density * 6);
            int dp3 = (int) (res.getDisplayMetrics().density * 3);
            for (int i = 0; i < amount; ++i) {
                AppCompatImageView v = new AppCompatImageView(root.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                lp.leftMargin = lp.rightMargin = dp3;
                root.addView(v, lp);
            }
        }
        reset();
    }

    public void reset() {
        int amount = root.getChildCount();
        if (amount < 1)
            return;

        for (int i = 1; i < amount; ++i) {
            setDrawable(i, R.drawable.ic_index_mark);
        }
        setDrawable(0, R.drawable.ic_index_mark_current);
        currentMark = 0;
    }

    public void setCurrentMark(int index) {
        int amount = root.getChildCount();
        if (index > amount)
            return;

        if (index > currentMark) {
            for (int i = currentMark; i < index; ++i) {
                setDrawable(i, R.drawable.ic_index_mark_done);
            }
            setDrawable(index, R.drawable.ic_index_mark_current);
            currentMark = index;
        }
    }

    public void setFailed() {
        int amount = root.getChildCount();
        if (amount < 1)
            return;

        for (int i = 0; i < currentMark; ++i) {
            setDrawable(i, R.drawable.ic_index_mark_done_failed);
        }
        setDrawable(currentMark, R.drawable.ic_index_mark_current_failed);
        for (int i = currentMark + 1; i < amount; ++i) {
            setDrawable(i, R.drawable.ic_index_mark_failed);
        }
    }

    private void setDrawable(int index, int id) {
        Drawable dr = AppCompatResources.getDrawable(root.getContext(), id);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

        View v = root.getChildAt(index);
        if (v instanceof ImageView) {
            ((ImageView) v).setImageDrawable(dr);
            if (dr instanceof Animatable) {
                ((Animatable) dr).start();
            }
        }
    }
}
