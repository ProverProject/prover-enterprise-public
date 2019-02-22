package io.prover.swypeid.util;

import android.media.Image;

public class FrameCreator {
    private long startTimestamp = 0;

    public Frame obtain(Image image) {
        if (startTimestamp == 0) {
            startTimestamp = image.getTimestamp();
        }
        Frame frame = Frame.obtain(image);
        long timestamp = image.getTimestamp() - startTimestamp;
        timestamp = timestamp / 1_000_000; // to ms;
        frame.timeStamp = (int) timestamp;
        return frame;
    }
}
