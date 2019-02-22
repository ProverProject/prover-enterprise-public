package io.prover.swypeid.camera2;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;

/**
 * Created by babay on 19.11.2017.
 */

public class OrientationHelper {
    private static final int[] DEFAULT_ORIENTATIONS = new int[4];
    private static final int[] INVERSE_ORIENTATIONS = new int[4];
    private static final int[] ZERO_ORIENTATIONS = new int[4];
    private static final int[] UPSIDE_DOWN_ORIENTATIONS = new int[4];

    static {
        DEFAULT_ORIENTATIONS[ROTATION_0] = 90;
        DEFAULT_ORIENTATIONS[ROTATION_90] = 0;
        DEFAULT_ORIENTATIONS[ROTATION_180] = 270;
        DEFAULT_ORIENTATIONS[ROTATION_270] = 180;

        INVERSE_ORIENTATIONS[ROTATION_0] = 270;
        INVERSE_ORIENTATIONS[ROTATION_90] = 180;
        INVERSE_ORIENTATIONS[ROTATION_180] = 90;
        INVERSE_ORIENTATIONS[ROTATION_270] = 0;

        ZERO_ORIENTATIONS[ROTATION_0] = 0;
        ZERO_ORIENTATIONS[ROTATION_90] = 270;
        ZERO_ORIENTATIONS[ROTATION_180] = 180;
        ZERO_ORIENTATIONS[ROTATION_270] = 90;

        ZERO_ORIENTATIONS[ROTATION_0] = 180;
        ZERO_ORIENTATIONS[ROTATION_90] = 90;
        ZERO_ORIENTATIONS[ROTATION_180] = 0;
        ZERO_ORIENTATIONS[ROTATION_270] = 270;
    }

    public static int getOrientationHint(@SensorOrientation int sensorOrientation, @SurfaceRotation int rotation) {
        return (sensorOrientation + 360 - 90 * rotation) % 360;
        /*switch (sensorOrientation) {
            case SensorOrientation.ZERO:
                return ZERO_ORIENTATIONS[rotation];

            case SensorOrientation.DEFAULT_DEGREES:
                return DEFAULT_ORIENTATIONS[rotation];

            case SensorOrientation.UPSIDE_DOWN:
                return UPSIDE_DOWN_ORIENTATIONS[rotation];

            case SensorOrientation.INVERSE_DEGREES:
                return INVERSE_ORIENTATIONS[rotation];
        }
        throw new RuntimeException(String.format("Not implemented for sensorOrientation: %d, rotation: %d", sensorOrientation, rotation));*/
    }

    public static int getRotationAngle(int rotation) {
        switch (rotation) {
            case ROTATION_0:
                return 0;
            case ROTATION_90:
                return 90;
            case ROTATION_180:
                return 180;
            case ROTATION_270:
                return 270;
        }
        return 0;
    }

    @IntDef({ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SurfaceRotation {
    }

    @IntDef({SensorOrientation.ZERO, SensorOrientation.DEFAULT_DEGREES, SensorOrientation.UPSIDE_DOWN, SensorOrientation.INVERSE_DEGREES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SensorOrientation {
        int ZERO = 0;
        int DEFAULT_DEGREES = 90;
        int UPSIDE_DOWN = 180;
        int INVERSE_DEGREES = 270;
    }
}
