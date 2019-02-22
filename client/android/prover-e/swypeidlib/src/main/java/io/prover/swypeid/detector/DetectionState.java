package io.prover.swypeid.detector;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import io.prover.swypeidlib.BuildConfig;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by babay on 11.11.2017.
 */

public class DetectionState {
    public final State state;
    public final int index;
    public final int x;
    public final int y;
    public final int d;
    public final int timestamp;

    @Message
    public final int message;

    DetectionState(int[] source, int timestamp) {
        state = State.values()[source[0]];
        index = source[1];
        x = source[2];
        y = source[3];
        message = source[4];
        d = source[5];
        this.timestamp = timestamp;
    }

    DetectionState() {
        state = State.WaitingForCode;
        index = 0;
        x = 0;
        y = 0;
        d = 0;
        message = 0;
        timestamp = 0;
    }

    public DetectionState(long[] source, int timestamp) {
        state = State.values()[(int) source[0]];
        index = (int) source[1];
        x = (int) source[2];
        y = (int) source[3];
        message = (int) source[4];
        d = (int) source[5];
        this.timestamp = timestamp;
    }

    public boolean isEqualsArray(int[] arr) {
        if (BuildConfig.DEBUG) {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3] && message == arr[4] && d == arr[5];
        } else {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3] && message == arr[4];
        }
    }

    public boolean isEqualsArray(long[] arr) {
        if (BuildConfig.DEBUG) {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3] && d == arr[4];
        } else {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectionState)) return false;

        DetectionState that = (DetectionState) o;

        if (state != that.state) return false;
        if (index != that.index) return false;
        if (x != that.x) return false;
        if (y != that.y) return false;
        return !BuildConfig.DEBUG || d == that.d;
    }

    @Override
    public int hashCode() {
        int result = state.ordinal();
        result = 31 * result + index;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + d;
        return result;
    }

    public enum State {WaitingForCode, WaitingForCircle, WaitingToStartSwypeCode, DetectingSwypeCode, SwypeCodeDone}

    @IntDef({
            Message.NONE,
            Message.LOW_CONTRAST,
            Message.SWYPE_FAIL_OUT_OF_BOUNDS,
            Message.SWYPE_FAIL_TIMEOUT
    })
    @Retention(SOURCE)
    public @interface Message {
        int NONE = 0;
        int LOW_CONTRAST = 1;
        int SWYPE_FAIL_OUT_OF_BOUNDS = 2;
        int SWYPE_FAIL_TIMEOUT = 3;
    }
}
