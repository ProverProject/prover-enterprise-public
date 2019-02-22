package io.prover.swypeid.camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.prover.swypeid.model.CameraRecordConfig;
import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.util.FrameCreator;
import io.prover.swypeidlib.Const;
import io.prover.swypeidlib.R;

/**
 * Created by babay on 09.12.2016.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyCamera2 implements ImageReader.OnImageAvailableListener {
    private final CameraStateListener mCameraStateLisneter;
    private final Camera2PrefsHelper camera2PrefsHelper = new Camera2PrefsHelper();
    private final SwypeIdMission mission;
    private final Context context;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private volatile VideoSessionWrapper mVideoSessionWrapper;

    private Camera2Config camera2Config;
    private FrameCreator frameCreator;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mVideoSessionWrapper = new VideoSessionWrapper(mCameraDevice, mission);
            mCameraStateLisneter.onCameraOpened(cameraDevice);
        }


        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            VideoSessionWrapper session = mVideoSessionWrapper;
            if (session != null) {
                session.closeVideoSession();
                session.onCameraDeviceClosed();
            }
            mCameraDevice = null;

            mVideoSessionWrapper = null;
            mCameraStateLisneter.onCameraDisconnected(cameraDevice);
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            VideoSessionWrapper session = mVideoSessionWrapper;
            if (session != null) {
                session.closeVideoSession();
                session.onCameraDeviceClosed();
            }

            cameraDevice.close();
            mCameraDevice = null;
            mVideoSessionWrapper = null;
            mCameraStateLisneter.onCameraError(cameraDevice, error);
            mission.onFailedToStartRecord();
        }
    };

    public MyCamera2(CameraStateListener mCameraStateLisneter, SwypeIdMission mission, Context context) {
        this.mCameraStateLisneter = mCameraStateLisneter;
        this.mission = mission;
        this.context = context.getApplicationContext();
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressLint("MissingPermission")
    @SuppressWarnings("MissingPermission")
    public void openCamera(Activity activity, Handler backgroundHandler, Size surfaceSize, Size selectedSize) {
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        try {
            Log.d(Const.TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (mCameraId == null)
                mCameraId = camera2PrefsHelper.selectCamera(manager, false);

            camera2Config = new Camera2Config(manager, mCameraId);
            camera2Config.selectResolutions(surfaceSize, selectedSize, activity);
            mission.video.onGotCamera2Config(camera2Config);
            mission.video.onSelectedVideoResolutionChanged(camera2Config.videoSize);
            manager.openCamera(mCameraId, mStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(activity.getString(R.string.camera_error))
                    .show(activity.getFragmentManager(), "dialog");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void openNextCamera(Activity activity, Handler backgroundHandler, Size surfaceSize, Size selectedSize) {
        new Thread(() -> {
            closeCamera(true);

            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            if (manager == null)
                return;

            try {
                mCameraId = camera2PrefsHelper.selectNextCameraId(mCameraId, manager);
            } catch (CameraAccessException e) {
                Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            openCamera(activity, backgroundHandler, surfaceSize, selectedSize);
        }).start();
    }

    public void setCameraId(String mCameraId) {
        this.mCameraId = mCameraId;
    }

    public void setResolution(Size surfaceSize, Size selectedSize, Context context) {
        if (mCameraId == null || camera2Config == null)
            return;

        try {
            camera2Config.selectResolutions(surfaceSize, selectedSize, context);
            mission.video.onSelectedVideoResolutionChanged(camera2Config.videoSize);
        } catch (CameraAccessException e) {
            Log.e(Const.TAG, e.getMessage(), e);
        }
    }

    public Size getPreviewSize() {
        return camera2Config.previewSize;
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    public void closeCamera(boolean waitForClosed) {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mVideoSessionWrapper) {
                if (waitForClosed)
                    mVideoSessionWrapper.closeVideoSessionSync();
                else
                    mVideoSessionWrapper.closeVideoSession();
                mVideoSessionWrapper.onCameraDeviceClosed();
                mVideoSessionWrapper = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public void closeCameraAsync(Runnable closedCallback) {
        new Thread(() -> {
            closeCamera(true);
            closedCallback.run();
        }).start();
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    public void startPreview(Handler backgroundHandler, SurfaceTexture screenTexture, SurfaceTexture rendererTexture) {
        if (mVideoSessionWrapper == null || camera2Config == null)
            return;
        if (mImageReader == null) {
            mImageReader = camera2Config.imageReader(4);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        screenTexture.setDefaultBufferSize(camera2Config.previewSize.width, camera2Config.previewSize.height);
        if (rendererTexture != null) {
            rendererTexture.setDefaultBufferSize(camera2Config.videoSize.width, camera2Config.videoSize.height);
        }

        Surface[] surfaces = rendererTexture == null ?
                new Surface[]{new Surface(screenTexture) /*, mImageReader.getSurface() */}
                : new Surface[]{new Surface(screenTexture), new Surface(rendererTexture) /*, mImageReader.getSurface()*/};

        Runnable startedNotificator = () -> mission.video.onPreviewStart(camera2Config.cameraResolutions, camera2Config.videoSize);
        frameCreator = new FrameCreator();
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, surfaces);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startVideoRecordingSession(SurfaceTexture texture, Surface mediaRecorderSurface, Handler backgroundHandler, Activity activity) {
        if (mVideoSessionWrapper == null || camera2Config == null)
            return;
        if (mImageReader == null) {
            mImageReader = camera2Config.imageReader(4);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        @OrientationHelper.SurfaceRotation int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final CameraRecordConfig recordConfig = new CameraRecordConfig(camera2Config, rotation);
        texture.setDefaultBufferSize(camera2Config.previewSize.width, camera2Config.previewSize.height);

        Runnable startedNotificator = () -> mission.onRecordingStart(recordConfig);
        frameCreator = new FrameCreator();
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, new Surface(texture), mediaRecorderSurface, mImageReader.getSurface());
        } catch (CameraAccessException e) {
            Log.e(Const.TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        try {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                if (frameCreator != null)
                    mission.detector.onFrameAvailable(frameCreator.obtain(image));
                else
                    image.close();
            }
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage(), e);
        }
    }


    public Size getVideoSize() {
        return camera2Config.videoSize;
    }

    public Integer getSensorOrientation() {
        return camera2Config.sensorOrientation;
    }

    public void stopVideoSession() {
        if (mVideoSessionWrapper != null) {
            mVideoSessionWrapper.closeVideoSession();
        }
        frameCreator = null;
    }

    public Camera2Config getCameraConfig() {
        return camera2Config;
    }

    public boolean isCameraOpen() {
        return mCameraDevice != null;
    }

    public void selectCameraAndOpen(boolean isFront, Activity activity, Handler backgroundHandler, Size surfaceSize, Size selectedSize) {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        try {
            mCameraId = camera2PrefsHelper.selectCamera(manager, isFront);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        new Thread(() -> {
            closeCamera(true);
            openCamera(activity, backgroundHandler, surfaceSize, selectedSize);
        }).start();
    }

    public interface CameraStateListener {
        void onCameraOpened(@NonNull CameraDevice cameraDevice);

        void onCameraDisconnected(@NonNull CameraDevice cameraDevice);

        void onCameraError(@NonNull CameraDevice cameraDevice, int error);
    }
}