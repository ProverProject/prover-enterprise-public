package io.prover.swypeid.camera2;

import android.util.DisplayMetrics;

/**
 * Created by babay on 16.11.2017.
 */

public enum Orientation {
    Landscape, Portrait;

    public static Orientation screenOrientation(DisplayMetrics dm) {
        return dm.widthPixels > dm.heightPixels ? Landscape : Portrait;
    }

    public static Orientation ofFrame(int width, int height) {
        return width > height ? Landscape : Portrait;
    }
}
