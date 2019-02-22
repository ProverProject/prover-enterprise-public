package io.prover.swypeid.viewholder;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import io.prover.common.util.MatrSolver;
import io.prover.swypeid.camera2.Camera2Config;
import io.prover.swypeid.detector.DetectionState;
import io.prover.swypeid.detector.DetectionStateChange;
import io.prover.swypeid.model.SwypeCode;
import io.prover.swypeid.model.SwypeIdDetector;
import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.model.SwypePoint;
import io.prover.swypeid.util.AnimateHelper;
import io.prover.swypeid.util.SimpleAnimatorListener;
import io.prover.swypeidlib.R;
import io.prover.swypeidlib.Settings;

public class SwypeViewHolderV2 implements ISwypeViewHolder, SwypeIdDetector.OnDetectionStateCahngedListener, View.OnLayoutChangeListener, SwypeIdDetector.OnSwypeCodeSetListener {

    private final TargetCalculator targetCalc = new TargetCalculator();
    private final Handler handler = new Handler();
    private final Resources res;
    private final ConstraintLayout root;

    private final ImageView arrowView[] = new ImageView[4];
    private final ImageView imageViews[] = new ImageView[7];
    private final ImageView centerView;
    private final ImageView targetView;
    private final ImageView target2View;
    private final SwypeViewMarksHolder indexMarks;
    private final SwypeIdMission mission;
    private DetectionState latestState;
    private SwypeCode swypeCode;
    private int centerRadius;
    private int targetRadius;
    private int orientationHint;

    private SwypeViewHolderV2(ConstraintLayout root, SwypeIdMission mission) {
        this.res = root.getResources();
        this.root = root;
        this.mission = mission;
        centerView = root.findViewById(R.id.swypePointCurrent);
        targetView = root.findViewById(R.id.swypePointTarget);
        target2View = root.findViewById(R.id.swypePointTarget2);
        indexMarks = new SwypeViewMarksHolder(root.findViewById(R.id.indexMarks));

        arrowView[0] = root.findViewById(R.id.swypeArrow1);
        arrowView[1] = root.findViewById(R.id.swypeArrow2);
        arrowView[2] = root.findViewById(R.id.swypeArrow3);
        arrowView[3] = root.findViewById(R.id.swypeArrow4);

        System.arraycopy(arrowView, 0, imageViews, 0, 4);
        imageViews[4] = centerView;
        imageViews[5] = targetView;
        imageViews[6] = target2View;

        mission.detector.detectionState.add(this);
        mission.detector.swypeCodeSet.add(this);
        root.setVisibility(View.GONE);

        root.addOnLayoutChangeListener(this);
    }

    public static SwypeViewHolderV2 inflate(ViewGroup parent, SwypeIdMission swypeIdModel) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.swype_v2, parent, false);
        SwypeViewHolderV2 holder = new SwypeViewHolderV2(view, swypeIdModel);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        parent.addView(view, lp);
        return holder;
    }

    private static double getAngle(double x, double y) {
        double angle;
        if (x == 0) {
            angle = y > 0 ? 90 : 270;
        } else {
            double k = y / x;
            angle = Math.atan(k) * 180 / Math.PI;
            if (x < 0)
                angle += 180.0f;
            angle = (angle + 360.0) % 360.0;
        }
        return angle;
    }

    @Override
    @MainThread
    public void onDetectionStateChanged(@NonNull DetectionStateChange stateChange) {
        latestState = stateChange.state;

        switch (stateChange.event) {
            case CircleDetected:
                if (swypeCode != null) {
                    indexMarks.setMarksAmount(swypeCode.length());
                    show();
                    setSwypeIndex(0, false);
                    updatePositionForCurrentState();
                    animateAppearAll();
                }
                break;

            case FailedSwypeCode:
                hideFailed();
                break;

            case NextSwypeCodeIndex:
                indexMarks.setCurrentMark(stateChange.state.index - 1);
                animateTargetReappear(stateChange);
                setSwypeIndex(stateChange.state.index - 1, true);
                updatePositionForCurrentState();
                break;

            case StartSwypeCode:
                updatePositionForCurrentState();
                break;

            case Nothing:
                if (stateChange.state.state == DetectionState.State.DetectingSwypeCode) {
                    updatePositionForCurrentState();
                }
                break;

            case CompletedSwypeCode:
                indexMarks.setCurrentMark(stateChange.state.index - 1);
                handler.postDelayed(this::hide, 1000);

                moveToCenter(targetView, 300);
                targetView.setImageResource(R.drawable.ic_swype_v2_target_fill);
                centerView.setImageResource(R.drawable.ic_swype_v2_center_disappear);
                Drawable dr = targetView.getDrawable();
                if (dr instanceof Animatable)
                    ((Animatable) dr).start();
                dr = centerView.getDrawable();
                if (dr instanceof Animatable)
                    ((Animatable) dr).start();

                break;
        }
    }

    private void moveToCenter(View v, int duration) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(v, "translationX", v.getTranslationX(), 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(v, "translationY", v.getTranslationY(), 0);
        animX.setDuration(duration);
        animY.setDuration(duration);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.start();
    }

    private void animateSlowdown(View v, @NonNull final DetectionStateChange change, int targetX, int targetY, final int durationMs) {
        targetCalc.set(change.state.x, change.state.y);
        final float x0 = targetCalc.x, y0 = targetCalc.y;
        targetCalc.set(change.oldState.x, change.oldState.y);
        final float prevX = targetCalc.x, prevY = targetCalc.y;
        final int deltaTimeMs = change.state.timestamp - change.oldState.timestamp;

        final float vX = (x0 - prevX) / deltaTimeMs;
        final float vY = (y0 - prevY) / deltaTimeMs;

        //v + a *t + b *t*t/2 == 0;
        //x0 + v*t + a*t*t/2 + b*t*t*t/6 == targetX;

        int t = durationMs;
        float t2 = t * t / 2;
        float t3 = t2 * t / 3;

        MatrSolver solver = new MatrSolver();
        solver.solve(t, t2, -vX, t2, t3, targetX - x0 - vX * t);
        float aX = (float) solver.x0, bX = (float) solver.x1;
        solver.solve(t, t2, -vY, t2, t3, targetY - y0 - vY * t);
        float aY = (float) solver.x0, bY = (float) solver.x1;

        final TimeAnimator animator = new TimeAnimator();
        animator.setDuration(durationMs);
        animator.setTimeListener((animation, totalTime, deltaTime1) -> {
            if (totalTime <= durationMs) {
                float T2 = totalTime * totalTime / 2;
                float T3 = T2 * totalTime / 3;
                float x = x0 + vX * totalTime + aX * T2 + bX * T3;
                float y = y0 + vY * totalTime + aY * T2 + bY * T3;
                v.setTranslationX(x);
                v.setTranslationY(y);
            } else {
                animation.end();
            }
        });
        animator.start();
    }

    private void setSwypeIndex(int index, boolean animate) {
        targetCalc.setDirection(swypeCode.getDirectionAtIndex(index));
        targetView.setTranslationX(targetCalc.x);
        targetView.setTranslationY(targetCalc.y);
    }

    @Override
    public void toggleDefectView() {

    }

    @Override
    @MainThread
    public void hide() {
        if (root.getVisibility() == View.VISIBLE) {
            ViewParent v = root.getParent();
            if (v instanceof ViewGroup)
                TransitionManager.beginDelayedTransition((ViewGroup) v);
            root.setVisibility(View.GONE);
            mission.swypeInterfaceVisibilityChangeListener.notifyEvent(false);
        }
    }

    @MainThread
    private void show() {
        centerView.setImageResource(R.drawable.ic_swype_v2_center_appear);
        for (int i = 0, arrowViewLength = arrowView.length; i < arrowViewLength; i++) {
            arrowView[i].setImageResource(R.drawable.ic_swype_v2_arr_v1);
        }

        ViewParent v = root.getParent();
        if (v instanceof ViewGroup)
            TransitionManager.beginDelayedTransition((ViewGroup) v);

        root.setVisibility(View.VISIBLE);
        mission.swypeInterfaceVisibilityChangeListener.notifyEvent(true);
    }

    private void hideFailed() {
        centerView.setImageResource(R.drawable.ic_swype_v2_center_fail);
        targetView.setImageResource(R.drawable.ic_swype_v2_target_fail);
        target2View.setImageResource(R.drawable.ic_swype_v2_target_fail);
        for (int i = 0, arrowViewLength = arrowView.length; i < arrowViewLength; i++) {
            arrowView[i].setImageResource(R.drawable.ic_swype_v2_arrow_fail);
        }

        boolean addedCallback = false;
        for (int i = 0; i < imageViews.length; i++) {
            Drawable dr = imageViews[i].getDrawable();
            if (dr instanceof Animatable) {
                ((Animatable) dr).start();
                if (!addedCallback) {
                    if (Build.VERSION.SDK_INT >= 23 && dr instanceof Animatable2) {
                        ((Animatable2) dr).registerAnimationCallback(AnimateHelper.callbackOfRunnables(null, this::hide));
                        addedCallback = true;
                    } else if (dr instanceof Animatable2Compat) {
                        ((Animatable2Compat) dr).registerAnimationCallback(AnimateHelper.callbackOfRunnablesCompat(null, this::hide));
                        addedCallback = true;
                    }
                }
            }
        }
        indexMarks.setFailed();

        if (!addedCallback)
            handler.postDelayed(this::hide, 1100);
    }

    private void updatePositionForCurrentState() {
        targetCalc.set(latestState.x, latestState.y);
        float x = targetCalc.x, y = targetCalc.y;
        targetView.setTranslationX(x);
        targetView.setTranslationY(y);

        int len = (int) Math.sqrt(x * x + y * y);

        int availSize = len - centerRadius - targetRadius;
        int arrowDistance = (int) (res.getDisplayMetrics().density * 12);
        int visibleArrows = Math.min(4, availSize / arrowDistance);
        if (visibleArrows > 0) {
            if (availSize >= arrowDistance * 5) {
                arrowDistance = availSize / 5;
            }
            double angle = getAngle(x, y);
            double cos = Math.cos(angle / 180 * Math.PI);
            double sin = Math.sin(angle / 180 * Math.PI);

            int stepX = (int) (arrowDistance * cos);
            int stepY = (int) (arrowDistance * sin);

            int startX = (int) (cos * (centerRadius));
            int startY = (int) (sin * (centerRadius));
            int endX = (int) (x - cos * targetRadius);
            int endY = (int) (y - sin * targetRadius);
            startX = (startX + endX) / 2 - (int) (stepX * visibleArrows / 2f);
            startY = (startY + endY) / 2 - (int) (stepY * visibleArrows / 2f);

            for (int i = 0; i < visibleArrows; ++i) {
                View v = arrowView[4 - visibleArrows + i];
                v.setTranslationX(startX + stepX * i);
                v.setTranslationY(startY + stepY * i);
                v.setRotation((float) angle + 45);
                v.setVisibility(View.VISIBLE);
            }
        } else
            visibleArrows = 0;
        for (int i = 0; i < 4 - visibleArrows; ++i) {
            arrowView[i].setVisibility(View.GONE);
        }
    }

    private void animateAppearAll() {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(centerView, "alpha", 0, 1);
        anim1.setDuration(300);

        targetView.setVisibility(View.INVISIBLE);
        target2View.setVisibility(View.GONE);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(prepareArrowsAnimator(), appearArrowsAnimator(150));
        set.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateTargetAppear();
            }
        });
        set.start();
    }

    private Animator prepareArrowsAnimator() {
        ObjectAnimator anims[] = new ObjectAnimator[4];
        for (int i = 0; i < anims.length; i++) {
            anims[i] = ObjectAnimator.ofFloat(arrowView[i], "alpha", 0);
            anims[i].setDuration(0);
        }
        AnimatorSet prepareSet = new AnimatorSet();
        prepareSet.playTogether(anims);
        return prepareSet;
    }

    private AnimatorSet appearArrowsAnimator(int singleArrowDuration) {
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(arrowView[0], "alpha", 0, 1, 0.25f);
        anim2.setDuration(singleArrowDuration);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(arrowView[1], "alpha", 0, 1, 0.5f);
        anim3.setDuration(singleArrowDuration);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(arrowView[2], "alpha", 0, 1, 0.75f);
        anim4.setDuration(singleArrowDuration);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(arrowView[3], "alpha", 0, 1);
        anim5.setDuration(singleArrowDuration);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(anim2, anim3, anim4, anim5);
        return set;
    }

    private void animateTargetAppear() {
        targetView.setVisibility(View.VISIBLE);
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(root.getContext(), R.drawable.ic_swype_v2_target_appear);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        targetView.setImageDrawable(dr);
        dr.start();
    }

    private void animateTargetReappear(@NonNull DetectionStateChange stateChange) {
        target2View.setVisibility(View.VISIBLE);
        target2View.setTranslationX(targetView.getTranslationX());
        target2View.setTranslationY(targetView.getTranslationY());

        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(root.getContext(), R.drawable.ic_swype_v2_target_disappear);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        target2View.setImageDrawable(dr);
        dr.start();
        dr.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                target2View.setVisibility(View.GONE);
            }
        });
        animateSlowdown(target2View, stateChange, 0, 0, 150);
        //moveToCenter(target2View, 200);

        animateTargetAppear();

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(prepareArrowsAnimator(), appearArrowsAnimator(25));
        set.start();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        targetCalc.onSizeChange(right - left, bottom - top, orientationHint);

        centerRadius = centerView.getWidth() / 2;
        targetRadius = targetView.getWidth() / 2;

        if (latestState != null) {
            switch (latestState.state) {
                case DetectingSwypeCode:
                case WaitingToStartSwypeCode:
                    setSwypeIndex(latestState.index - 1, false);
                    updatePositionForCurrentState();
                    break;
            }
        }
    }

    @Override
    public void onSwypeCodeSet(SwypeCode swypeCode, SwypeCode actualSwypeCode, Integer orientationHint) {
        this.swypeCode = swypeCode;
        Camera2Config config = mission.camera.getCameraConfig();
        if (config != null && config.isFrontCamera) {
            if (Settings.FORE_CAMERA_FLIP_X)
                this.swypeCode = this.swypeCode.flipHorizontal();
            if (Settings.FORE_CAMERA_FLIP_Y)
                this.swypeCode = this.swypeCode.flipVertical();

            targetCalc.setFlipHorizontal(Settings.FORE_CAMERA_FLIP_X);
            targetCalc.setFlipVertical(Settings.FORE_CAMERA_FLIP_Y);
        } else {
            targetCalc.setFlipHorizontal(false);
            targetCalc.setFlipVertical(false);
        }
        this.orientationHint = orientationHint;
    }

    private static class TargetCalculator {
        final float point[] = new float[2];
        final Matrix rotateScaleMatrix = new Matrix();
        int targetX, targetY;
        float x, y;
        int sizeMul;
        boolean flipVertical = false;
        boolean flipHorizontal = false;
        int orientation;

        void onSizeChange(int width, int height, int orientation) {
            this.orientation = orientation;
            sizeMul = Math.min(width, height) * 5 / 16;
            updateMatrix();
        }

        public void setFlipVertical(boolean flipVertical) {
            this.flipVertical = flipVertical;
            updateMatrix();
        }

        public void setFlipHorizontal(boolean flipHorizontal) {
            this.flipHorizontal = flipHorizontal;
        }

        public void set(int x, int y) {
            point[0] = x;
            point[1] = y;
            rotateScaleMatrix.mapPoints(point);
            this.x = targetX - point[0];
            this.y = targetY - point[1];
        }

        public void setDirection(int direction) {
            SwypePoint point = SwypePoint.fromDirection(direction);
            point.y = -point.y;

            x = targetX = point.x * sizeMul;
            y = targetY = point.y * sizeMul;
        }

        private void updateMatrix() {
            rotateScaleMatrix.reset();
            rotateScaleMatrix.postScale(1, 1);
            rotateScaleMatrix.postRotate(orientation, 0, 0);
            if (flipVertical) {
                rotateScaleMatrix.postScale(1, -1);
            }
            if (flipHorizontal) {
                rotateScaleMatrix.postScale(-1, 1);
            }

            rotateScaleMatrix.postScale(sizeMul / 1024f, sizeMul / 1024f);
        }
    }
}