package io.prover.swypeid.camera2;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.util.FrameRateCounter;
import io.prover.swypeid.util.TaskSyncWaiter;
import io.prover.swypeidlib.Const;

import static io.prover.common.Const.TAG;

/**
 * Created by babay on 19.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class VideoSessionWrapper {
    private final SwypeIdMission mission;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 10);
    private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            float resultFps = fpsCounter.addFrame();
            if (resultFps >= 0) {
                mission.onVideoFpsUpdate(resultFps);
            }
        }
    };
    private final TaskSyncWaiter sessionClosedWaiter = new TaskSyncWaiter();
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCameraVideoSession;
    private Handler mBackgroundHandler;

    VideoSessionWrapper(CameraDevice cameraDevice, SwypeIdMission mission) {
        this.mCameraDevice = cameraDevice;
        this.mission = mission;
    }

    /**
     * synchronized stop video session
     */
    boolean closeVideoSessionSync() {
        if (mCameraVideoSession != null) {
            if (closeVideoSession()) {
                Log.d(TAG, "closeVideoSessionSync: waiting onClosedSession");
                sessionClosedWaiter.waitForDone(2500);
                Log.d(TAG, "closeVideoSessionSync: done waiting onClosedSession");
                return true;
            }
        }
        return false;
    }

    boolean closeVideoSession() {
        if (mCameraVideoSession != null) {
            CameraCaptureSession session;
            synchronized (this) {
                session = mCameraVideoSession;
                mCameraVideoSession = null;
            }

            if (session != null) {
                sessionClosedWaiter.setDone(false);
                if (mBackgroundHandler != null && mBackgroundHandler.getLooper().getThread().isAlive()) {
                    mBackgroundHandler.post(() -> doCloseSession(session));
                } else {
                    doCloseSession(session);
                }
            }
            return true;
        }
        return false;
    }

    private void doCloseSession(CameraCaptureSession session) {
        try {
            session.close();
        } catch (Exception e) {
            Log.e(TAG, "close session error: " + e.getMessage(), e);
        }
    }

    void startVideoSession(Handler backgroundHandler, Runnable startedNotificator, Surface... surfaces) throws CameraAccessException {
        this.mBackgroundHandler = backgroundHandler;
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        List<Surface> surfaceList = new ArrayList<>();

        for (Surface surface : surfaces) {
            surfaceList.add(surface);
            mPreviewRequestBuilder.addTarget(surface);
        }

        // Start a capture session
        // Once the session starts, we can update the UI and start recording
        mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                synchronized (this) {
                    if (mCameraDevice == null)
                        return;
                }

                mCameraVideoSession = cameraCaptureSession;
                try {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    mCameraVideoSession.setRepeatingRequest(mPreviewRequestBuilder.build(), captureCallback, backgroundHandler);
                } catch (CameraAccessException | IllegalStateException e) {
                    Log.e(Const.TAG, "onConfigured: ", e);
                }
                if (startedNotificator != null)
                    startedNotificator.run();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                mission.onFailedToStartRecord();
            }

            @Override
            public void onClosed(@NonNull CameraCaptureSession session) {
                Log.d(TAG, "onClosedSession: ");

                synchronized (VideoSessionWrapper.this) {
                    mCameraVideoSession = null;
                    sessionClosedWaiter.setDone(true);
                }
            }

        }, backgroundHandler);
    }

    void onCameraDeviceClosed() {
        synchronized (this) {
            mCameraDevice = null;
        }
    }
}
