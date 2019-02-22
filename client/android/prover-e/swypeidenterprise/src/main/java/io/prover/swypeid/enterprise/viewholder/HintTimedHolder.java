package io.prover.swypeid.enterprise.viewholder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.prover.swypeid.enterprise.R;
import io.prover.swypeid.enterprise.viewholder.model.Hint;
import io.prover.swypeid.util.SimpleAnimatorListener;
import io.prover.swypeid.viewholder.ImageViewHolder;

public class HintTimedHolder extends ImageViewHolder implements IHintHolder {

    @NonNull
    protected final ConstraintLayout root;
    @NonNull
    protected final Hint hint;
    @NonNull
    protected final HintListener hintListener;
    protected final Handler handler = new Handler();
    protected final TextView textView;
    protected HideHintRunnable hideRunnable;
    private boolean showShort;

    private Animator textAnimator;

    public HintTimedHolder(@NonNull ConstraintLayout root, @NonNull Hint hint, @NonNull HintListener hintListener) {
        super(root.findViewById(R.id.largeImageNotification));
        this.root = root;
        this.hint = hint;
        this.hintListener = hintListener;

        textView = root.findViewById(hint.useTopHintView ? R.id.hintText : R.id.hintText2);
    }

    public HintTimedHolder(ImageView imageView, @NonNull ConstraintLayout root, @NonNull Hint hint, @NonNull HintListener hintListener) {
        super(imageView);
        this.root = root;
        this.hint = hint;
        this.hintListener = hintListener;
        textView = root.findViewById(hint.useTopHintView ? R.id.hintText : R.id.hintText2);
    }

    @Override
    public void show() {
        if (hint.animationTime == 0 && (hint.hasImage() || hint.animate))
            TransitionManager.beginDelayedTransition(root);

        if (hint.hasMessage()) {
            showTextHint();
        }

        if (hint.hasImage()) {
            setDrawable(hint.drawableId);
            imageView.setVisibility(View.VISIBLE);
        }

        hideRunnable = new HideHintRunnable(hint, () -> {
            hideRunnable = null;
            hintListener.onHintHidden(this);
        });
        if (showShort)
            hideRunnable.postAsap();
        else
            hideRunnable.post();
        //hideRunnable = new HideHintRunnable(hideRunnable, hint.minHintVisibleTime);

    }

    public void cancel() {
        hintListener.onHintHidden(this);
        if (hideRunnable != null && !hideRunnable.cancelled) {
            if (hideRunnable.imageState == HintState.Visible || hideRunnable.textState == HintState.Visible) {
                hideRunnable.cancel();
                if (hint.hasImage())
                    imageView.setVisibility(View.GONE);
                if (hint.hasMessage())
                    textView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void hide() {
        if (hideRunnable == null) {
            hintListener.onHintHidden(this);
        } else if (hideRunnable.imageState == HintState.Visible || hideRunnable.textState == HintState.Visible) {
            if (hideRunnable.cancelled)
                return;
            hideRunnable.postAsap();
        }
    }

    public void setShowShort() {
        showShort = true;
    }

    @Override
    public HintState getState() {
        if (hideRunnable == null)
            return HintState.Hidden;
        if (hideRunnable.imageState == HintState.Visible || hideRunnable.textState == HintState.Visible)
            return HintState.Visible;
        if (hideRunnable.imageState == HintState.Hiding || hideRunnable.textState == HintState.Hiding)
            return HintState.Hiding;
        return HintState.Hidden;
    }

    protected void showTextHint() {
        if (hint.useTopHintView) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
            if (hint.anchor == 0) {
                lp.topToBottom = R.id.balanceContainer;
                lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
            } else if (hint.isAbove) {
                lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                lp.bottomToTop = hint.anchor;
            } else {
                lp.topToBottom = hint.anchor;
                lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
            }
            textView.setLayoutParams(lp);
        }

        if (hint.messageId != 0)
            textView.setText(hint.messageId);
        else if (hint.message != null)
            textView.setText(hint.message);
        textView.setVisibility(View.VISIBLE);
        if (hint.animationTime > 0) {
            startTextAnimator(true);
        }
    }

    private void startTextAnimator(boolean appear) {
        Animator anim = textAnimator;
        if (anim != null)
            anim.cancel();
        textView.setVisibility(View.VISIBLE);
        textAnimator = ObjectAnimator.ofFloat(textView, "alpha", appear ? 0 : 1, appear ? 1 : 0);
        textAnimator.setDuration(hint.animationTime);
        textAnimator.start();
        if (!appear) {
            textAnimator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    textView.setVisibility(View.GONE);
                    textView.setAlpha(1);
                }
            });
        }
    }

    boolean isHiding() {
        return hideRunnable != null && (hideRunnable.imageState == HintState.Hiding || hideRunnable.textState == HintState.Hiding);
    }

    protected class HideHintRunnable {
        final long startTime;
        final long hideMessageDelay;
        final long hideImageDelay;
        final Hint hint;
        private final Runnable runWhenHidden;
        public boolean hideImage;
        public boolean hideText;
        protected HintState imageState;
        protected HintState textState;
        boolean cancelled;

        public HideHintRunnable(Hint hint, Runnable runWhenHidden) {
            this(hint, hint.hasImage() && hint.imageTimeout != Integer.MAX_VALUE,
                    hint.hasMessage() && hint.textTimeout != Integer.MAX_VALUE, runWhenHidden);
        }

        public HideHintRunnable(Hint hint, boolean hideImage, boolean hideText, Runnable runWhenHidden) {
            this.hint = hint;
            this.runWhenHidden = runWhenHidden;
            startTime = System.currentTimeMillis();
            hideMessageDelay = hint.textTimeout;
            hideImageDelay = hint.imageTimeout;
            this.hideImage = hideImage;
            this.hideText = hideText;
            imageState = hint.hasImage() ? HintState.Visible : HintState.Hidden;
            textState = hint.hasMessage() ? HintState.Visible : HintState.Hidden;
        }

        HideHintRunnable post() {
            if (imageState == HintState.Visible && textState == HintState.Visible && hideImage && hideText) {
                handler.postDelayed(this::hideBoth, hideImageDelay);
            } else {
                if (imageState == HintState.Visible && hideImage)
                    handler.postDelayed(this::hideImage, hideImageDelay);

                if (textState == HintState.Visible && hideText)
                    handler.postDelayed(this::hideMessage, hideMessageDelay);
            }
            return this;
        }

        HideHintRunnable postAsap() {
            long timeHintWasVisible = System.currentTimeMillis() - hideRunnable.startTime;
            long delay = Math.max(0, hint.minHintVisibleTime - timeHintWasVisible);
            if (imageState == HintState.Visible && textState == HintState.Visible && hideImage && hideText) {
                handler.postDelayed(this::hideBoth, delay);
            } else {
                if (imageState == HintState.Visible && hideImage)
                    handler.postDelayed(this::hideImage, delay);

                if (textState == HintState.Visible && hideText)
                    handler.postDelayed(this::hideMessage, delay);
            }
            return this;
        }

        void hideImage() {
            if (cancelled || imageState.isHidingOrHidden())
                return;

            imageState = HintState.Hiding;
            TransitionManager.beginDelayedTransition(root);
            imageView.setVisibility(View.GONE);
            handler.postDelayed(this::onImageHidden, 500);
        }

        void hideMessage() {
            if (cancelled || textState.isHidingOrHidden())
                return;

            textState = HintState.Hiding;
            if (hint.animationTime == 0) {
                TransitionManager.beginDelayedTransition(root);
                textView.setVisibility(View.GONE);
            } else {
                startTextAnimator(false);
            }
            handler.postDelayed(this::onTextHidden, hint.animationTime == 0 ? 500 : hint.animationTime);
        }

        void hideBoth() {
            if (cancelled || (imageState.isHidingOrHidden() && textState.isHidingOrHidden()))
                return;

            imageState = textState = HintState.Hiding;
            TransitionManager.beginDelayedTransition(root);
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            handler.postDelayed(this::onImageHidden, 500);
            handler.postDelayed(this::onTextHidden, 500);
        }

        void onImageHidden() {
            if (cancelled || imageState == HintState.Hidden)
                return;

            imageState = HintState.Hidden;
            if (textState == HintState.Hidden && runWhenHidden != null) {
                runWhenHidden.run();
            }
        }

        void onTextHidden() {
            if (cancelled || textState == HintState.Hidden)
                return;

            textState = HintState.Hidden;
            if (imageState == HintState.Hidden && runWhenHidden != null) {
                runWhenHidden.run();
            }
        }

        public void cancel() {
            cancelled = true;
        }
    }
}
