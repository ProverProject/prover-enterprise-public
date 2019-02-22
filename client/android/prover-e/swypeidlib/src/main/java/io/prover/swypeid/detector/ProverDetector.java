package io.prover.swypeid.detector;

import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Locale;

import io.prover.common.view.ScreenLogger;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.model.SwypeCode;
import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.util.Frame;
import io.prover.swypeid.util.FrameRateCounter;
import io.prover.swypeidlib.Settings;

/**
 * Created by babay on 11.11.2017.
 */

public class ProverDetector {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final int[] detectionResult = new int[6];
    private final SwypeIdMission mission;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 3);
    private DetectionState detectionState;
    private long nativeHandler;
    private SwypeCode swypeCode;
    private byte[] planeY, planeU, planeV;

    ProverDetector(SwypeIdMission mission) {
        this.mission = mission;
    }

    public void init(Size videoSize, Size detectorSize) {
        if (nativeHandler == 0) {
            nativeHandler = initSwype(videoSize.ratio, detectorSize.width, detectorSize.height);
        }
        mission.setSwypeVersion(getSwypeHelperVersion(nativeHandler));
    }

    public synchronized void setSwype(@NonNull SwypeCode swype) {
        this.swypeCode = swype;
        updateSwype();
        Log.d("ProverMVPDetector", String.format("Set swype code %s", swype));
    }

    private void updateSwype() {
        if (nativeHandler != 0) {
            setSwype(nativeHandler, swypeCode.getCodeV2());
        }
    }

    public synchronized void release() {
        if (nativeHandler != 0) {
            releaseNativeHandler(nativeHandler);
            nativeHandler = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void detectFrame(Frame frame) {
        int width = frame.width;
        int height = frame.height;

        int timeToAnalyze = 0;

        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            if (frame.image != null) {

                Image.Plane planeY = frame.image.getPlanes()[0];
                /*Image.Plane planeU = frame.image.getPlanes()[1];
                Image.Plane planeV = frame.image.getPlanes()[2];
                if (temp == null || temp.length != width * height * 4)
                    temp = new byte[width * height * 4];
                detectFrameColored(nativeHandler,
                        planeY.getBuffer(), planeY.getRowStride(), planeY.getPixelStride(),
                        planeU.getBuffer(), planeU.getRowStride(), planeU.getPixelStride(),
                        planeV.getBuffer(), planeV.getRowStride(), planeV.getPixelStride(),
                        width, height, frame.timeStamp, detectionResult, temp);
                if (saveFrame > 0) {
                    BitmapHelper.saveGrayscale(temp, width, height, "temp" + saveFrame + ".png");
                    --saveFrame;
                }//*/
                detectFrameY_8BufStrided(nativeHandler, planeY.getBuffer(), planeY.getRowStride(), planeY.getPixelStride(), width, height, frame.timeStamp, detectionResult);
            } else if (frame.data != null) {
                detectFrameNV21(nativeHandler, frame.data, width, height, frame.timeStamp, detectionResult);
            }


            timeToAnalyze = (int) (System.currentTimeMillis() - time);

/*            if (BuildConfig.DEBUG)
                Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));*/
            if (Settings.DETAILED_DETECTOR_LOG) {
                int rowStride = frame.image == null ? width : frame.image.getPlanes()[0].getRowStride();
                int pixelStride = frame.image == null ? width : frame.image.getPlanes()[0].getPixelStride();
                int bufSize = frame.image == null ? frame.data.length : frame.image.getPlanes()[0].getBuffer().limit();
                float dx = detectionResult[2] / 1024f;
                float dy = detectionResult[3] / 1024f;
                String text = String.format(Locale.getDefault(), "Detector: %dx%d=%d, f%d(%d,%d) %d,%d,%+.3f,%+.3f,%d, %d ms",
                        width, height, bufSize, frame.format, rowStride, pixelStride,
                        detectionResult[0], detectionResult[1], dx, dy, detectionResult[5],
                        System.currentTimeMillis() - time);
                mission.getLogger().addToScreenLog(text, ScreenLogger.MessageType.DETECTOR);
            }
        }
        detectionDone(frame.timeStamp, timeToAnalyze);
    }

    private void detectionDone(int timestamp, int timeToAnalyze) {
        if (detectionState == null) {
            detectionState = new DetectionState(detectionResult, timestamp);
        } else if (!detectionState.isEqualsArray(detectionResult)) {
            final DetectionState oldState = detectionState;
            detectionState = new DetectionState(detectionResult, timestamp);
            mission.detector.notifyDetectionStateChanged(new DetectionStateChange(oldState, detectionState, timeToAnalyze));
            /*if (oldState.state == DetectingSwypeCode && detectionState.state == Waiting) {
                updateSwype();
            }*/
        }
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            mission.detector.onDetectorFpsUpdate(fps);
        }
    }

    /**
     * initialize swype with specific fps and swype code
     *
     * @param videoAspectRatio
     * @param detectorWidth
     * @param detectorHeight
     */
    private native long initSwype(float videoAspectRatio, int detectorWidth, int detectorHeight);

    /**
     * set swype code
     *
     * @param nativeHandler
     * @param swype
     */
    private native void setSwype(long nativeHandler, String swype);

    private void ensureBuffers(int width, int height) {
        int size = width * height;
        if (planeY == null || planeY.length != size)
            planeY = new byte[size];
        size = size / 4;
        if (planeU == null || planeU.length != size)
            planeU = new byte[size];
        if (planeV == null || planeV.length != size)
            planeV = new byte[size];
    }

    private void parsePaddedPlane(Image.Plane plane, int width, int height, byte[] result) {
        ByteBuffer planeBuf = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        parsePaddedPlane(planeBuf, rowStride, pixelStride, width, height, result);
    }

    /**
     * detect single frame
     *
     * @param frameData
     * @param width
     * @param height
     * @param result    -- an array with 4 items: State, Index, X, Y
     */
    private native void detectFrameNV21(long nativeHandler, byte[] frameData, int width, int height, int timestamp, int[] result);

    private native void detectFrameY_8BufStrided(long nativeHandler, ByteBuffer planeY, int rowStride, int pixelStride, int width, int height, int timestamp, int[] result);

    private native void detectFrameColored(long nativeHandler, ByteBuffer planeY, int yRowStride, int yPixelStride,
                                           ByteBuffer planeU, int uRowStride, int uPixelStride,
                                           ByteBuffer planeV, int vRowStride, int vPixelStride,
                                           int width, int height, int timestamp, int[] result, @Nullable byte[] debugImage);

    private native void releaseNativeHandler(long nativeHandler);

    private native void parsePaddedPlane(ByteBuffer plane, int rowStride, int pixelStride, int width, int height, byte[] result);

    private native void yuvToRgb(byte[] y, byte[] u, byte[] v, int[] rgb, int width, int height);

    private native int getSwypeHelperVersion(long nativeHandler);
}
