package io.prover.swypeid.util;

import android.animation.TimeAnimator;
import android.view.View;

public class ViewSlowdownAnimator {

    public void animateSlowdown(View v, float x0, float y0, int prevX, float prevY, int deltaTime, int targetX, int targetY, int duration) {
        final float vX = (x0 - prevX) / deltaTime;
        final float vY = (y0 - prevY) / deltaTime;
        int t = duration;

        final float aX = 2 * (targetX - x0 - vX * t) / t / t;
        final float aY = 2 * (targetY - y0 - vY * t) / t / t;

        TimeAnimator animator = new TimeAnimator();
        animator.setDuration(duration);

        animator.setTimeListener((animation, totalTime, deltaTime1) -> {
            float t2 = totalTime * totalTime / 2;
            float x = x0 + vX * totalTime + aX * t2;
            float y = y0 + vY * totalTime + aY * t2;
            v.setTranslationX(x);
            v.setTranslationY(y);
        });
    }


}
