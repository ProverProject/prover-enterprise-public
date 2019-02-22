package io.prover.swypeid.model;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import io.prover.common.controller.IScreenLogger;
import io.prover.common.controller.ListenerList;
import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList1Sync;
import io.prover.common.controller.ListenerList2;
import io.prover.common.controller.MissionModel;
import io.prover.common.controller.RootModel;
import io.prover.common.permissions.PermissionManager;
import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.TransportModel;
import io.prover.swypeidlib.Settings;

import static io.prover.swypeid.model.SwypeIdMission.ControllerState.NOT_READY;
import static io.prover.swypeid.model.SwypeIdMission.ControllerState.READY;

public class SwypeIdMission extends MissionModel {
    private static final String KEY_SELECTED_CAMERA = "cameraId";
    private static final long MAX_BUSY_TIME = 3000;

    public final ListenerList2<OnFpsUpdateListener, Float, Float> fpsUpdateListener
            = new ListenerList2<>(mainHandler, OnFpsUpdateListener::OnFpsUpdate);

    public final ListenerList<SwypeIdMission.PostprocessingStartedListener> postprocessingListener =
            new ListenerList<>(mainHandler, PostprocessingStartedListener::onPostprocessingStartedListener);

    public final ListenerList<SwypeIdMission.SwypeCodeRequestListener> swypeCodeRequestingListener =
            new ListenerList<>(mainHandler, SwypeCodeRequestListener::onRequestingSwypeCode);

    public final ListenerList1<SwypeIdMission.VideoPostedToBlockchainListener, Boolean> videoPostedToBlockchainListener =
            new ListenerList1<>(mainHandler, VideoPostedToBlockchainListener::onVideoPostedToBlockchain);

    public final ListenerList1Sync<SwypeInterfaceVisibilityChangeListener, Boolean> swypeInterfaceVisibilityChangeListener =
            new ListenerList1Sync<>(SwypeInterfaceVisibilityChangeListener::onSwypeInterfaceVisibilityChange);


    public final VideoRecorder video;
    public final SwypeIdDetector detector = new SwypeIdDetector();
    public final Camera2Model camera;
    /**
     * collects video metadata
     */
    private final VideoMetadataCollector metadataCollector;
    private final Activity activity;
    public TemplatesModel templates;
    /**
     * video post-processing: inject metadata and calculate file hash
     */
    private volatile VideoPostRecordingProcessor postProcessor;
    private volatile boolean recording;
    /**
     * time of start some important task
     * (starting video, preparing video recorder, stopping video)
     * zero if no important task
     */
    private long busyStartTime = 0;
    /**
     * holds primary video data (resolutions etc)
     */
    private SwypeVideoData videoData;

    @MainThread
    public SwypeIdMission(Activity activity, TransportModel transport) {
        super(transport, new SwypeIdLogger(transport));
        this.activity = activity;
        metadataCollector = new VideoMetadataCollector(this);
        video = new VideoRecorder(logger);
        templates = new TemplatesModel(this, activity);

        ((SwypeIdLogger) logger).setMission(this);
        camera = new Camera2Model(activity, this);
        transport.onSwypeCodeOrderComplete.add(this::onSwypeCodeOrderComplete);
    }

    @MainThread
    public void startRecording() {
        if (PermissionManager.checkHaveWriteSdcardPermission(activity)) {
            if (canStartMainTask() && isNotBusy()) {
                busyStartTime = System.currentTimeMillis();
                camera.startRecording();
            }
        } else {
            PermissionManager.ensureHaveWriteSdcardPermission(activity, () ->
                    mainHandler.postDelayed(this::startRecording, 1000));
        }
    }

    @MainThread
    public void requestFinishRecording(boolean cancel) {
        if (isNotBusy()) {
            busyStartTime = System.currentTimeMillis();
            camera.requestRecordingFinish(cancel);
        }
    }

    @AnyThread
    public void onRecordingStart(CameraRecordConfig cameraRecordConfig) {
        runOnUiThread(() -> {
            recording = true;
            videoData = new SwypeVideoData(cameraRecordConfig);
            video.onRecordingStart.postNotifyEvent(cameraRecordConfig);
            detector.onRecordingStart(this, cameraRecordConfig);
            requestSwypeCode();
        });
    }

    @AnyThread
    public void onRecordingStop(Context context, File file) {
        if (!recording)
            return;
        recording = false;
        boolean isVideoConfirmed = detector.isVideoConfirmed();
        SwypeVideoData videoData = this.videoData;
        if (file != null && videoData != null) {
            postprocessingListener.postNotifyEvent();
            videoData.onRecordingStop(file, isVideoConfirmed);
            Map<String, String> meta = metadataCollector.getMetadata(videoData);
            postProcessor = new VideoPostRecordingProcessor(context, this::onDoneVideoPostProcessing, videoData, meta);
            transport.requestAmountHandler.onRequestAmountChanged(transport.getTotalExecutingRequests());
            runOnUiThread(() -> setState(SwypeIdMission.ControllerState.POSTPROCESSING));

            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                postProcessor.execute();
            } else
                postProcessor.run();
        } else {
            runOnUiThread(this::onFinishedMainTask);
            video.onRecordingStop.postNotifyEvent(null, isVideoConfirmed);
        }
    }

    @MainThread
    private void requestSwypeCode() {
        swypeCodeRequestingListener.postNotifyEvent();
        if (transport.requestSwypeCode()) {
            transport.setBeReadyToStart(false);
        } else {
            transport.setBeReadyToStart(true);
            mainHandler.postDelayed(() -> requestFinishRecording(false), 1000);
        }
    }

    @MainThread
    private void onDoneVideoPostProcessing(VideoPostRecordingProcessor processor, Exception exception) {
        if (processor.videoData.file.length() == 0) {
            mainHandler.post(() -> {
                if (activity != null)
                    Toast.makeText(activity, "Warning! file is zero-sized after processor", Toast.LENGTH_SHORT).show();
            });
        }
        postProcessor = null;
        SwypeVideoData videoData = processor.videoData;
        if (exception != null) {
            generalErrorListener.postNotifyEvent(exception, null);
        }
        video.onRecordingStop.postNotifyEvent(videoData.file, videoData.isVideoConfirmed);

        if (videoData.canSend()) {
            if (videoData.isVideoConfirmed) {
                transport.postFileHash(videoData.swypeCodeOrder, videoData.fileDigest);
            } else if (videoData.isSendHashNoSwype()) {
                transport.postFileHashNoSwype(videoData.fileDigest);
            } else {
                logger.addToScreenLog("Swype not confirmed. Not sending hash.", IScreenLogger.MessageType.GENERAL);
                onFinishedMainTask();
            }
        } else {
            if (videoData.fileDigest != null)
                generalErrorListener.postNotifyEvent(new Exception("error sending file hash: no offer or offer outdated"), null);
            onFinishedMainTask();
        }
        transport.requestAmountHandler.onRequestAmountChanged(transport.getTotalExecutingRequests());
    }

    @AnyThread
    public void beforeRecordingStop() {
        detector.killDetector();
    }

    @AnyThread
    public void setSwypeVersion(int swypeVersion) {
        SwypeVideoData vd = videoData;
        if (vd != null)
            vd.swypeVersion = swypeVersion;
    }

    @AnyThread
    public void onVideoFpsUpdate(float fps) {
        fpsUpdateListener.postNotifyEvent(fps, detector.detectorFps);
    }

    @Override
    @MainThread
    public boolean canStartMainTask() {
        return postProcessor == null && videoData == null && (Settings.FAKE_SWYPE_CODE || super.canStartMainTask());
    }

    @Override
    @MainThread
    public void onFinishedMainTask() {
        super.onFinishedMainTask();
        videoData = null;
    }

    @Override
    @MainThread
    public void onOrderConfirmed(@NonNull OrderType orderType) {
        if (orderType == OrderType.FileHash) {
            videoPostedToBlockchainListener.postNotifyEvent(false);
            onFinishedMainTask();
        }
    }

    @MainThread
    private void onSwypeCodeOrderComplete(ISwypeCodeOrderResult result, long timeSpent) {
        if (videoData != null && result != null) {
            videoData.setSwypeCodeOrder(result);
            detector.swypeCodeSet.postNotifyEvent(videoData.swypeCode, videoData.actualSwypeCode, videoData.recordConfig.orientationHint);
        }
    }

    @Override
    @MainThread
    public void onOrderRequestFailed(@NonNull OrderType orderType, Exception e) {
        switch (orderType) {
            case SwypeFast:
            case SwypeFull:
            case SwypeServerDefault:
                if (videoData != null && recording) {
                    transport.setBeReadyToStart(true);
                    requestFinishRecording(false);
                }
                break;

            case FileHash:
            case HashNoSwype:
                //TODO: do resend
                onFinishedMainTask();
                break;
        }
    }

    @Override
    @MainThread
    protected void onChangeState(@SwypeIdMission.ControllerState int oldState, @SwypeIdMission.ControllerState int newState) {
        super.onChangeState(oldState, newState);
        switch (newState) {
            case READY:
                videoData = null;
                detector.resetLatestState();
                break;

            case NOT_READY:
                videoData = null;
                detector.resetLatestState();
                break;
        }
    }

    @Override
    @MainThread
    public void onChangeActivityState(int state, int oldState, Activity activity) {
        super.onChangeActivityState(state, oldState, activity);
        if (state == RootModel.ActivityState.CREATED && oldState == RootModel.ActivityState.RESUMED) {
            busyStartTime = 0;
            if (video.isRecording())
                camera.requestRecordingFinish(false);
            camera.onPause(activity);
        }
    }

    @MainThread
    boolean isNotBusy() {
        return (System.currentTimeMillis() - busyStartTime) > MAX_BUSY_TIME;
    }

    @AnyThread
    public void onFailedToStartRecord() {
        //busyStartTime = 0;
    }

    @MainThread
    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SELECTED_CAMERA, camera.getCameraConfig().getCameraId());
        return bundle;
    }

    @MainThread
    public void loadInstanceState(Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_SELECTED_CAMERA)) {
            camera.selectCameraAndOpen(bundle.getString(KEY_SELECTED_CAMERA));
        }
    }

    @MainThread
    public void setPostHashNoSwype(boolean sendHashNoSwype) {
        if (videoData != null)
            videoData.setPostHashNoSwype(sendHashNoSwype);
    }

    @MainThread
    public boolean isSendHashNoSwype() {
        return videoData != null && videoData.isSendHashNoSwype();
    }

    @AnyThread
    public boolean isRecording() {
        return recording;
    }

    @MainThread
    public interface OnFpsUpdateListener {
        void OnFpsUpdate(float fps, float processorFps);
    }

    @MainThread
    public interface PostprocessingStartedListener {
        void onPostprocessingStartedListener();
    }

    @MainThread
    public interface SwypeCodeRequestListener {
        void onRequestingSwypeCode();
    }

    @MainThread
    public interface VideoPostedToBlockchainListener {
        /**
         * @param confirmed -- if true, then we know that transaction already added to blockchain
         */
        void onVideoPostedToBlockchain(boolean confirmed);
    }

    @MainThread
    public interface SwypeInterfaceVisibilityChangeListener {
        void onSwypeInterfaceVisibilityChange(boolean visible);
    }

    @IntDef({
            SwypeIdMission.ControllerState.NOT_READY,
            SwypeIdMission.ControllerState.READY,
            SwypeIdMission.ControllerState.RECORDING,
            SwypeIdMission.ControllerState.POSTPROCESSING,
    })
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerState {
        int NOT_READY = MissionStateBase.NOT_READY;
        int READY = MissionStateBase.READY;
        int RECORDING = 2;
        int POSTPROCESSING = 3;
    }
}
