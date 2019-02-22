package io.prover.swypeid.enterprise.viewholder.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public class Hint {
    private static final int MIN_HINT_VISIBLE_TIME = 1500;

    public final int messageId;
    public final String message;
    public final int drawableId;
    public final int minifyDrawableId;
    public final int minifiedDrawableId;
    public final int startDelay;
    public final int imageTimeout;
    public final int textTimeout;
    public final int animationTime;
    public final int minifiedDelay;
    public final int minifiedRepeatCount;

    public final int anchor;
    public final boolean isAbove;
    public final boolean animate;
    @NonNull
    public final Hints hint;

    public final boolean useTopHintView;
    public final boolean forcedHint;
    public final boolean isWarning;
    public final int minHintVisibleTime;


    public Hint(@NonNull Hints hint, int messageId, int drawableId, int minifyDrawableId, int minifiedDrawableId, int startDelay, int imageTimeout, int textTimeout, int animationTime, int minifiedDelay, int minifiedRepeatCount, int anchor, boolean isAbove, boolean animate, boolean useTopHintView, boolean forcedHint, boolean isWarning, int minHintVisibleTime) {
        this.hint = hint;
        this.messageId = messageId;
        this.startDelay = startDelay;
        this.animationTime = animationTime;
        this.useTopHintView = useTopHintView;
        this.forcedHint = forcedHint;
        this.isWarning = isWarning;
        this.minHintVisibleTime = minHintVisibleTime;
        this.message = null;
        this.drawableId = drawableId;
        this.minifyDrawableId = minifyDrawableId;
        this.minifiedDrawableId = minifiedDrawableId;
        this.imageTimeout = imageTimeout;
        this.textTimeout = textTimeout;
        this.minifiedDelay = minifiedDelay;
        this.minifiedRepeatCount = minifiedRepeatCount;
        this.anchor = anchor;
        this.isAbove = isAbove;
        this.animate = animate;
    }

    public Hint(@NonNull Hints hint, int messageId, int drawableId, int minifyDrawableId, int minifiedDrawableId, int startDelay, int imageTimeout, int textTimeout, int animationTime, int minifiedDelay, int minifiedRepeatCount, boolean useTopHintView, boolean forcedHint, boolean isWarning, int minHintVisibleTime) {
        this.hint = hint;
        this.messageId = messageId;
        this.startDelay = startDelay;
        this.animationTime = animationTime;
        this.useTopHintView = useTopHintView;
        this.forcedHint = forcedHint;
        this.isWarning = isWarning;
        this.minHintVisibleTime = minHintVisibleTime;
        this.message = null;
        this.drawableId = drawableId;
        this.minifyDrawableId = minifyDrawableId;
        this.minifiedDrawableId = minifiedDrawableId;
        this.imageTimeout = imageTimeout;
        this.textTimeout = textTimeout;
        this.minifiedDelay = minifiedDelay;
        this.minifiedRepeatCount = minifiedRepeatCount;
        this.anchor = 0;
        this.isAbove = false;
        this.animate = true;
    }

    public boolean hasImage() {
        return drawableId != 0;
    }

    public boolean hasMessage() {
        return messageId != 0 || message != null;
    }

    public boolean isTimedHint() {
        return minifyDrawableId == 0 && textTimeout != Integer.MAX_VALUE;
    }


    public static class Builder {
        final Hints hint;
        @StringRes
        int messageId = 0;

        @DrawableRes
        int drawableId = 0;

        @DrawableRes
        int minifyDrawableId = 0;

        @DrawableRes
        int minifiedDrawableId = 0;

        @DrawableRes
        int minifiedRepeatCount = 0;

        @DrawableRes
        int minifiedDelay = 0;

        int imageTimeout = 0;
        int textTimeout = 0;
        int startDelay = 0;
        boolean useTopHintView = true;
        boolean forcedHint = false;
        boolean isWarning = false;
        int animationTime = 0;
        int minHintVisibleTime = Hint.MIN_HINT_VISIBLE_TIME;

        public Builder(Hints hint) {
            this.hint = hint;
        }

        public Hint build() {
            return new Hint(hint, messageId, drawableId, minifyDrawableId, minifiedDrawableId,
                    startDelay, imageTimeout, textTimeout, animationTime, minifiedDelay, minifiedRepeatCount, useTopHintView, forcedHint, isWarning, minHintVisibleTime);
        }

        public Builder setMessageId(@StringRes int messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setMessage(@StringRes int messageId, int timeout) {
            this.messageId = messageId;
            this.textTimeout = timeout;
            return this;
        }

        public Builder setDrawableId(@DrawableRes int drawableId) {
            this.drawableId = drawableId;
            return this;
        }

        public Builder setImageId(@DrawableRes int drawableId, int timeout) {
            this.drawableId = drawableId;
            this.imageTimeout = timeout;
            return this;
        }

        public Builder setMinifyDrawableId(@DrawableRes int minifyDrawableId) {
            this.minifyDrawableId = minifyDrawableId;
            return this;
        }

        public Builder setMinifiedDrawableId(@DrawableRes int minifiedDrawableId) {
            this.minifiedDrawableId = minifiedDrawableId;
            return this;
        }

        public Builder setMinifiedRepeatCount(int minifiedRepeatCount) {
            this.minifiedRepeatCount = minifiedRepeatCount;
            return this;
        }

        public Builder setMinifiedDelay(int minifiedDelay) {
            this.minifiedDelay = minifiedDelay;
            return this;
        }

        public Builder setImageTimeout(int imageTimeout) {
            this.imageTimeout = imageTimeout;
            return this;
        }

        public Builder setTextTimeout(int textTimeout) {
            this.textTimeout = textTimeout;
            return this;
        }

        public Builder setStartDelay(int startDelay) {
            this.startDelay = startDelay;
            return this;
        }

        public Builder setUseTopHintView(boolean useTopHintView) {
            this.useTopHintView = useTopHintView;
            return this;
        }

        public Builder setForcedHint(boolean forcedHint) {
            this.forcedHint = forcedHint;
            return this;
        }

        public Builder setWarning(boolean warning) {
            isWarning = warning;
            return this;
        }

        public Builder setAnimationTime(int animationTime) {
            this.animationTime = animationTime;
            return this;
        }

        public Builder setMinHintVisibleTime(int minHintVisibleTime) {
            this.minHintVisibleTime = minHintVisibleTime;
            return this;
        }
    }
}
