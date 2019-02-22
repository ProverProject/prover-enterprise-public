package io.prover.swypeid.detector;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.prover.swypeid.detector.DetectionState.State;

public class DetectionStateChange {
    @NonNull
    public final DetectionState oldState;
    @NonNull
    public final DetectionState state;

    public final Event event;

    public final int timeToAnalyze;

    DetectionStateChange(@Nullable DetectionState oldState, @NonNull DetectionState state, int timeToAnalyze) {
        this.oldState = oldState != null ? oldState : new DetectionState();
        this.state = state;
        this.timeToAnalyze = timeToAnalyze;

        if (oldState == null) {
            event = Event.StartDetection;
        } else if (state.state == State.WaitingToStartSwypeCode && oldState.state != State.WaitingToStartSwypeCode) {
            event = Event.CircleDetected;
        } else if (state.state == State.DetectingSwypeCode && oldState.state != State.DetectingSwypeCode) {
            event = Event.StartSwypeCode;
        } else if (state.state == State.DetectingSwypeCode && state.index > oldState.index) {
            event = Event.NextSwypeCodeIndex;
        } else if (oldState.state == State.DetectingSwypeCode && state.state == State.WaitingForCircle) {
            event = Event.FailedSwypeCode;
        } else if (oldState.state == State.DetectingSwypeCode && this.state.state == State.SwypeCodeDone) {
            event = Event.CompletedSwypeCode;
        } else {
            event = Event.Nothing;
        }
    }

    public enum Event {
        Nothing, StartDetection, CircleDetected, StartSwypeCode, NextSwypeCodeIndex, FailedSwypeCode, CompletedSwypeCode
    }
}
