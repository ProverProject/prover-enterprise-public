package io.prover.swypeid.model;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList2;
import io.prover.common.controller.Logger;
import io.prover.common.view.ScreenLogger;
import io.prover.swypeid.camera2.Camera2Config;
import io.prover.swypeid.camera2.CameraUtil;
import io.prover.swypeid.camera2.MyCamera2;
import io.prover.swypeid.camera2.OrientationHelper;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeidlib.Const;

import static io.prover.common.Const.TAG;

public class VideoRecorder {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList2<OnPreviewStartListener, List<Size>, Size> previewStart
            = new ListenerList2<>(handler, OnPreviewStartListener::onPreviewStart);

    public final ListenerList1<OnRecordingStartListener, CameraRecordConfig> onRecordingStart
            = new ListenerList1<>(handler, OnRecordingStartListener::onRecordingStart);

    public final ListenerList2<OnRecordingStopListener, File, Boolean> onRecordingStop
            = new ListenerList2<>(handler, OnRecordingStopListener::onRecordingStop);

    public final ListenerList1<OnRecorderResolutionChangedListener, Size> recorderResolutionChangedListener
            = new ListenerList1<>(handler, OnRecorderResolutionChangedListener::onRecorderResolutionChanged);
    private final Logger logger;
    public List<Size> availableVideoSizes;

    private volatile boolean recording = false;
    private volatile boolean mediaRecorderReady = false;
    private MediaRecorder mMediaRecorder;

    private File file;
    private List<Size> availableCaptureSizes;

    public VideoRecorder(Logger logger) {
        this.logger = logger;
    }

    public void prepare(MyCamera2 myCamera, Activity activity) {
        try {
            file = CameraUtil.getOutputMediaFile(CameraUtil.MEDIA_TYPE_VIDEO, activity, System.currentTimeMillis());
            if (file == null)
                return;
            if (file.exists())
                file = findSuitableFileName(file);
            if (mMediaRecorder == null)
                mMediaRecorder = new MediaRecorder();
            else
                mMediaRecorder.reset();

            Size videoSize = myCamera.getVideoSize();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(file.getPath());
            mMediaRecorder.setVideoEncodingBitRate(videoSize.getHighQualityBitRate());
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(videoSize.width, videoSize.height);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int orientationHint = OrientationHelper.getOrientationHint(myCamera.getSensorOrientation(), rotation);
            mMediaRecorder.setOrientationHint(orientationHint);

            mMediaRecorder.prepare();
            mediaRecorderReady = true;
            Log.d(Const.TAG, String.format("prepare video recorder: %dx%d, orientation: %d", videoSize.width, videoSize.height, orientationHint));
        } catch (IOException e) {
            Log.e(Const.TAG, "prepare: ", e);
        }
    }

    public void release() {
        recording = false;
        mediaRecorderReady = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void stop() {
        recording = false;
        mediaRecorderReady = false;
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                Log.e(TAG, "stop: ", e);
            }
            mMediaRecorder.reset();   // clear recorder configuration
        }
    }

    public void start() {
        if (isPrepared()) {
            recording = true;
            mediaRecorderReady = false;
            mMediaRecorder.start();
        }
    }

    public boolean isPrepared() {
        return mMediaRecorder != null && mediaRecorderReady;
    }

    public boolean isRecording() {
        return recording;
    }

    public Surface getSurface() {
        if (mMediaRecorder != null && (mediaRecorderReady || recording)) {
            try {
                return mMediaRecorder.getSurface();
            } catch (IllegalStateException e) {
                Log.e(Const.TAG, "video recorder" + e.getMessage(), e);
            }
        }
        return null;
    }

    public File getOutputFile() {
        return file;
    }

    public void clearOutputFile() {
        file = null;
    }

    public void onPreviewStart(List<Size> cameraResolutions, Size mVideoSize) {
        previewStart.postNotifyEvent(cameraResolutions, mVideoSize);
    }

    public void onSelectedVideoResolutionChanged(Size videoSize) {
        recorderResolutionChangedListener.postNotifyEvent(videoSize);
    }

    public List<Size> getAvailableVideoSizes() {
        return availableVideoSizes;
    }

    public List<Size> getAvailableCaptureSizes() {
        return availableCaptureSizes;
    }

    public void onGotCamera2Config(Camera2Config camera2Config) {
        this.availableCaptureSizes = camera2Config.captureResolutions;
        this.availableVideoSizes = camera2Config.cameraResolutions;
        logger.addToScreenLog("Available Capture Sizes: " + Size.toString(availableCaptureSizes), ScreenLogger.MessageType.GENERAL);
        logger.addToScreenLog("Sensor orientation: " + camera2Config.sensorOrientation, ScreenLogger.MessageType.GENERAL);
        StringBuilder builder = new StringBuilder("Available capture formats: ");
        int[] availableCaptureFormats = camera2Config.availableCaptureFormats;
        for (int i = 0; i < availableCaptureFormats.length; i++) {
            int format = availableCaptureFormats[i];
            builder.append("0x").append(Integer.toHexString(format)).append(", ");
        }

        logger.addToScreenLog(builder, ScreenLogger.MessageType.GENERAL);
    }

    private File findSuitableFileName(File file) {
        if (file.length() == 0) {
            file.delete();
            return file;
        }
        String parent = file.getParent();
        String name = file.getName();
        String ext;
        int dotPos = name.indexOf('.');
        if (dotPos >= 0) {
            ext = name.substring(dotPos);
            name = name.substring(0, dotPos);
        } else {
            ext = "";
        }

        for (int i = 0; ; ++i) {
            File f = new File(parent, name + '_' + i + ext);
            if (!f.exists())
                return f;
            else if (f.length() == 0) {
                f.delete();
                return f;
            }
        }

    }

    @MainThread
    public interface OnPreviewStartListener {
        void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize);
    }

    public interface OnRecordingStartListener {
        void onRecordingStart(CameraRecordConfig config);
    }

    @MainThread
    public interface OnRecordingStopListener {
        void onRecordingStop(File file, boolean isVideoConfirmed);
    }

    public interface OnRecorderResolutionChangedListener {
        void onRecorderResolutionChanged(Size recorderResolution);
    }
}
