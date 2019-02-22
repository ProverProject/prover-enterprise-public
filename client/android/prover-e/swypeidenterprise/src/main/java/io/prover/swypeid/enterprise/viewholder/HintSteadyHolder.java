package io.prover.swypeid.enterprise.viewholder;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import io.prover.swypeid.enterprise.R;
import io.prover.swypeid.enterprise.viewholder.model.Hint;
import io.prover.swypeid.enterprise.viewholder.model.Hints;

/**
 * Created by babay on 10.12.2017.
 */

public class HintSteadyHolder extends HintTimedHolder implements IHintHolder {

    private int minifiedRepeatCount;
    private HintState state = HintState.ExpectingToShow;

    public HintSteadyHolder(ConstraintLayout root, Hint hint, @NonNull HintListener hintListener) {
        super(root.findViewById(R.id.allDoneIcon), root, hint, hintListener);
        this.minifiedRepeatCount = hint.minifiedRepeatCount;
    }

    @Override
    public void show() {
        setState(HintState.ExpectingToShow);
        if (hint.startDelay == 0) {
            doShow();
        } else
            handler.postDelayed(this::doShow, hint.startDelay);
    }

    private void doShow() {
        if (state != HintState.ExpectingToShow)
            return;

        setState(HintState.Visible);
        if (hint.hasImage()) {
            imageView.setVisibility(View.GONE);
            centerImage();
        }
        if (hint.animationTime == 0)
            TransitionManager.beginDelayedTransition(root);
        if (hint.hasImage()) {
            setDrawable(hint.drawableId);
            imageView.setVisibility(View.VISIBLE);
            handler.postDelayed(this::animateMove, hint.imageTimeout);
        }
        showTextHint();
        postHideRunnable(true, false);
    }

    private void postHideRunnable(boolean hideText, boolean hideImage) {
        hideRunnable = new HideHintRunnable(hint, hideImage, hideText, () -> {
            hideRunnable = null;
            if (state == HintState.Minified) {
                hintListener.OnHintMinified(this);
            } else if (state == HintState.Hiding || state == HintState.Hidden) {
                setState(HintState.Hidden);
                hintListener.onHintHidden(this);
            }
        });
        hideRunnable.post();
    }

    @Override
    public void hide() {
        if (state == HintState.Hiding)
            return;
        if (state == HintState.Hidden) {
            hintListener.onHintHidden(this);
            return;
        }
        setState(HintState.Hiding);

        if (hideRunnable != null && hint.hasImage())
            hideRunnable.hideImage = true;

        super.hide();
        handler.postDelayed(() -> {
            setState(HintState.Hidden);
            //hintListener.onHintHidden(this);
        }, 500);
    }

    @Override
    public void cancel() {
        if (hint.hasImage()) {
            imageView.setVisibility(View.GONE);
            centerImage();
        }
        if (hint.hasMessage()) {
            textView.setVisibility(View.GONE);
            textView.setAlpha(1);
        }

        setState(HintState.Hidden);
        if (hideRunnable != null) {
            hideRunnable.cancel();
            hideRunnable = null;
        }
    }

    @Override
    public void setShowShort() {
    }

    @Override
    public HintState getState() {
        return state;
    }

    private void setState(HintState state) {
        this.state = state;

    }

    private void centerImage() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
        if (hint.useTopHintView) {
            lp.topToBottom = textView.getId();
            lp.topToTop = ConstraintLayout.LayoutParams.UNSET;
            lp.topMargin = (int) (imageView.getResources().getDisplayMetrics().density * 32);
        } else {
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topMargin = 0;
        }
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightMargin = 0;
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageView.setLayoutParams(lp);
    }

    private void layoutImageTopLeft() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
        lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lp.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        lp.rightMargin = lp.topMargin = (int) (imageView.getResources().getDisplayMetrics().density * 16);

        if (hint.hint == Hints.AllDone) {
            lp.height = (int) (imageView.getResources().getDisplayMetrics().density * 71);
            lp.width = (int) (imageView.getResources().getDisplayMetrics().density * 80);
        } else if (hint.hint == Hints.MakeCircular || hint.hint == Hints.SwypeCodeFailed) {
            lp.height = (int) (imageView.getResources().getDisplayMetrics().density * 80);
            lp.width = (int) (imageView.getResources().getDisplayMetrics().density * 80);
        }

        imageView.setLayoutParams(lp);
    }

    private void animateMove() {
        if (state != HintState.Visible || hint.minifyDrawableId == 0)
            return;
        setState(HintState.Minifying);
        setDrawable(hint.minifyDrawableId);
        TransitionManager.beginDelayedTransition((ViewGroup) imageView.getParent());
        layoutImageTopLeft();
        handler.postDelayed(() -> {
            if (state == HintState.Minifying) {
                setState(HintState.Minified);
                if (hideRunnable == null) {
                    hintListener.OnHintMinified(HintSteadyHolder.this);
                }
                postRepeatAnimationIfShould();
            }
        }, 500);
    }

    private void postRepeatAnimationIfShould() {
        if (minifiedRepeatCount != 0 && state == HintState.Minified && hint.minifiedDrawableId != 0) {
            handler.postDelayed(() -> {
                if (state != HintState.Minified)
                    return;
                setDrawable(hint.minifiedDrawableId);
                --minifiedRepeatCount;
                postRepeatAnimationIfShould();
            }, hint.minifiedDelay);
        }
    }
}
