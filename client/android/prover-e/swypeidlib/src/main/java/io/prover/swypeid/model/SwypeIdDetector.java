package io.prover.swypeid.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.prover.common.controller.ListenerList;
import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList3;
import io.prover.swypeid.detector.DetectionState.State;
import io.prover.swypeid.detector.DetectionStateChange;
import io.prover.swypeid.detector.SwypeDetectorHandler;
import io.prover.swypeid.util.Frame;

public class SwypeIdDetector {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList3<OnSwypeCodeSetListener, SwypeCode, SwypeCode, Integer> swypeCodeSet
            = new ListenerList3<>(handler, OnSwypeCodeSetListener::onSwypeCodeSet);

    public final ListenerList<SwypeCodeConfirmedListener> swypeCodeConfirmed
            = new ListenerList<>(handler, SwypeCodeConfirmedListener::onSwypeCodeConfirmed);

    public final ListenerList1<OnDetectionStateCahngedListener, DetectionStateChange> detectionState
            = new ListenerList1<>(handler, SwypeIdDetector.OnDetectionStateCahngedListener::onDetectionStateChanged);
    volatile float detectorFps;
    private SwypeDetectorHandler swypeDetectorHandler;

    @Nullable
    private State latestState;

    public void onRecordingStart(SwypeIdMission mission, CameraRecordConfig config) {
        swypeDetectorHandler = SwypeDetectorHandler.newHandler(config.videoSize, config.detectorSize, mission);
    }

    @AnyThread
    public void killDetector() {
        SwypeDetectorHandler sdh = swypeDetectorHandler;
        if (sdh != null) {
            sdh.sendQuit();
            swypeDetectorHandler = null;
        }
    }

    public void onFrameAvailable(Frame frame) {
        SwypeDetectorHandler sdh = swypeDetectorHandler;
        if (sdh == null) {
            frame.recycle();
        } else if (sdh.isAlive()) {
            sdh.onFrameAvailable(frame);
        } else {
            swypeDetectorHandler = null;
            frame.recycle();
        }
    }

    public void notifyDetectionStateChanged(@NonNull DetectionStateChange detectionStateChange) {
        latestState = detectionStateChange.state.state;
        detectionState.postNotifyEvent(detectionStateChange);
        if (detectionStateChange.state.state == State.SwypeCodeDone && detectionStateChange.oldState.state != State.SwypeCodeDone) {
            swypeCodeConfirmed.postNotifyEvent();
        }
    }

    public void onDetectorFpsUpdate(float fps) {
        detectorFps = fps;
    }

    public boolean isVideoConfirmed() {
        return latestState == State.SwypeCodeDone;
    }

    public void resetLatestState() {
        latestState = null;
    }

    @Nullable
    public State getLatestState() {
        return latestState;
    }

    @MainThread
    public interface OnDetectionStateCahngedListener {
        void onDetectionStateChanged(@NonNull DetectionStateChange stateChange);
    }

    public interface OnSwypeCodeSetListener {
        void onSwypeCodeSet(SwypeCode swypeCode, SwypeCode actualSwypeCode, Integer orientationHint);
    }

    public interface SwypeCodeConfirmedListener {
        void onSwypeCodeConfirmed();
    }
}
