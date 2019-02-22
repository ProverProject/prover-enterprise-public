package io.prover.swypeid.model;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;

import io.prover.common.controller.ListenerList;
import io.prover.common.controller.MissionModel;
import io.prover.common.controller.RootModel;
import io.prover.common.permissions.PermissionManager;
import io.prover.common.transport.TransportModel;
import io.prover.swypeid.camera2.AutoFitTextureView;
import io.prover.swypeid.camera2.Camera2Config;
import io.prover.swypeid.camera2.MyCamera2;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.viewholder.ISurfacesHolder;
import io.prover.swypeid.viewholder.SimpleSurfacesHolder;

import static io.prover.swypeidlib.Const.TAG;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Model implements MyCamera2.CameraStateListener, VideoRecorder.OnRecordingStartListener, ISurfacesHolder.SurfacesHolderListener, VideoRecorder.OnRecorderResolutionChangedListener {

    private final Handler foregroundHandler = new Handler(Looper.getMainLooper());

    public final ListenerList<OnSwitchingCameraListener> onSwitchingCameraListener
            = new ListenerList<>(foregroundHandler, OnSwitchingCameraListener::onSwitchingCamera);

    private final Activity activity;
    private final MyCamera2 myCamera;
    private final SwypeIdMission mission;

    private ISurfacesHolder surfacesHolder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size selectedSize;
    private boolean resumed;

    public Camera2Model(Activity activity, SwypeIdMission mission) {
        this.activity = activity;
        this.mission = mission;

        myCamera = new MyCamera2(this, mission, activity);
        mission.video.onRecordingStart.add(this);
        mission.video.recorderResolutionChangedListener.add(this);
    }

    public void setRootModel(RootModel<? extends TransportModel, ? extends MissionModel> swypeIdModel) {
        swypeIdModel.onActivityResume.add(this::onResume);
    }

    public void setTextureView(AutoFitTextureView textureView) {
        surfacesHolder = new SimpleSurfacesHolder(activity, textureView, mission.camera);
    }

    private void openCamera() {
        if (!PermissionManager.ensureHaveCameraPermission(activity, null))
            return;


        myCamera.openCamera(activity, mBackgroundHandler, surfacesHolder.getPreviewSurfaceSize(), selectedSize);
        selectedSize = myCamera.getVideoSize();
        surfacesHolder.setPreviewSize(myCamera.getPreviewSize());
    }

    private void startPreview() {
        Size size = myCamera.getPreviewSize();
        foregroundHandler.post(() -> surfacesHolder.configurePreview(activity, size));

        myCamera.startPreview(mBackgroundHandler, surfacesHolder.getPreviewSurfaceTexture(), surfacesHolder.getRendererInputTexture());
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void onPause(Activity activity) {
        resumed = false;
/*        //surfacesHolder.onPause();
        if (mission.video.isRecording()) {
            requestRecordingFinish(false);
        }*/
        mission.video.release();
        myCamera.closeCamera(true);
        stopBackgroundThread();
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        HandlerThread thread = mBackgroundThread;
        if (thread != null) {
            thread.quitSafely();
            try {
                thread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void onResume(Activity activity) {
        resumed = true;
        startBackgroundThread();
        surfacesHolder.onResume(activity);
    }

    @Override
    public void onCameraOpened(@NonNull CameraDevice cameraDevice) {
        startPreview();
    }

    @Override
    public void onCameraDisconnected(@NonNull CameraDevice cameraDevice) {

    }

    @Override
    public void onCameraError(@NonNull CameraDevice cameraDevice, int error) {
        Toast.makeText(activity, "Video error: " + error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onCameraError: ", new RuntimeException("Video error: " + error));
    }

    public void onActivityStop() {
        myCamera.closeCamera(false);
        mission.video.release();
    }

    boolean startRecording() {
        if (!surfacesHolder.isAvailable() || null == myCamera.getVideoSize() || mBackgroundThread == null || !mBackgroundThread.isAlive()) {
            return false;
        }
        mBackgroundHandler.post(() -> {
            mission.video.start();
            Surface recordSurface = mission.video.getSurface();
            if (recordSurface == null) {
                mission.video.prepare(myCamera, activity);
                mission.video.start();
                recordSurface = mission.video.getSurface();
            }
            if (!mission.video.isRecording()) {
                mission.onFailedToStartRecord();
                requestRecordingFinish(true);
                return;
            }

            SurfaceTexture texture = surfacesHolder.getPreviewSurfaceTexture();
            myCamera.startVideoRecordingSession(texture, recordSurface, mBackgroundHandler, activity);
        });
        return true;
    }

    public void setCameraResolution(Size size) {
        if (size == null || !size.equalsIgnoringRotation(selectedSize)) {
            selectedSize = size;
            if (surfacesHolder.getPreviewSurfaceSize() != null) {
                surfacesHolder.configurePreview(activity, size);
                myCamera.setResolution(surfacesHolder.getPreviewSurfaceSize(), selectedSize, activity);
                surfacesHolder.setPreviewSize(myCamera.getPreviewSize());
                myCamera.startPreview(mBackgroundHandler, surfacesHolder.getPreviewSurfaceTexture(), surfacesHolder.getRendererInputTexture());
            }
        }
    }

    @Override
    public void onRecorderResolutionChanged(Size recorderResolution) {
        if (resumed) {
            mission.video.prepare(myCamera, activity);
        }
    }

    @Override
    public void onRecordingStart(CameraRecordConfig config) {
        //mission.video.start();
    }

    @Override
    public void onSurfaceDestroyed() {
        myCamera.closeCamera(false);
    }

    @Override
    public void onReady() {
        openCamera();
    }

    public void openNextCamera() {
        if (!PermissionManager.ensureHaveCameraPermission(activity, null))
            return;

        onSwitchingCameraListener.postNotifyEvent();
        myCamera.openNextCamera(activity, mBackgroundHandler, surfacesHolder.getPreviewSurfaceSize(), selectedSize);
        selectedSize = myCamera.getVideoSize();
        surfacesHolder.setPreviewSize(myCamera.getPreviewSize());
    }

    @AnyThread
    void requestRecordingFinish(boolean cancel) {
        if (mission.video.isRecording()) {
            mission.beforeRecordingStop();
            Runnable r = () -> {
                if (resumed) {
                    myCamera.startPreview(mBackgroundHandler, surfacesHolder.getPreviewSurfaceTexture(), surfacesHolder.getRendererInputTexture());
                    stopRecorder(cancel);
                    mission.video.prepare(myCamera, activity);
                } else {
                    myCamera.stopVideoSession();
                    stopRecorder(cancel);
                }

            };
            new Thread(r, "threadStopRecorder").start();
        }
    }

    private void stopRecorder(boolean cancel) {
        mission.video.stop();
        File file = mission.video.getOutputFile();
        mission.video.clearOutputFile();
        if (!cancel && file.length() == 0) {
            foregroundHandler.post(() -> {
                if (activity != null)
                    Toast.makeText(activity, "Warning! file is zero-sized when finished", Toast.LENGTH_SHORT).show();
            });
        }
        if (cancel || file.length() == 0) {
            file.delete();
            file = null;
        }
        mission.onRecordingStop(activity, file);
    }

    public Camera2Config getCameraConfig() {
        return myCamera.getCameraConfig();
    }

    public void selectCameraAndOpen(String cameraId) {
        myCamera.setCameraId(cameraId);
    }

    public boolean isCameraOpen() {
        return myCamera.isCameraOpen();
    }

    public void selectCameraAndOpen(boolean isFront) {
        if (myCamera.isCameraOpen())
            onSwitchingCameraListener.postNotifyEvent();
        myCamera.selectCameraAndOpen(isFront, activity, mBackgroundHandler, surfacesHolder.getPreviewSurfaceSize(), selectedSize);
    }

    public interface OnSwitchingCameraListener {
        void onSwitchingCamera();
    }
}