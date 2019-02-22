package io.prover.swypeid.util;

import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.Animatable2Compat;

public class AnimateHelper {

    public static Animatable2Compat.AnimationCallback callbackOfRunnablesCompat(Runnable onStart, Runnable onEnd) {
        return new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationStart(Drawable drawable) {
                if (onStart != null)
                    onStart.run();
            }

            @Override
            public void onAnimationEnd(Drawable drawable) {
                if (onEnd != null)
                    onEnd.run();
            }
        };
    }

    public static Animatable2.AnimationCallback callbackOfRunnables(Runnable onStart, Runnable onEnd) {
        return new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationStart(Drawable drawable) {
                if (onStart != null)
                    onStart.run();
            }

            @Override
            public void onAnimationEnd(Drawable drawable) {
                if (onEnd != null)
                    onEnd.run();
            }
        };
    }
}
