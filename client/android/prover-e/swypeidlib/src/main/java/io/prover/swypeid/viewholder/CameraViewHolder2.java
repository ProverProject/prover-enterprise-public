package io.prover.swypeid.viewholder;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

import io.prover.common.controller.RootModel;
import io.prover.common.transport.TransportModel;
import io.prover.common.util.ScreenOrientationLock;
import io.prover.swypeid.camera2.AutoFitTextureView;
import io.prover.swypeid.model.CameraRecordConfig;
import io.prover.swypeid.model.SwypeIdMission;

public class CameraViewHolder2 implements ICameraViewHolder {

    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();

    private final Activity activity;
    private final FrameLayout mRoot;
    private final SwypeIdMission mission;

    public CameraViewHolder2(FrameLayout cameraContainerView, Activity activity, RootModel<? extends TransportModel, SwypeIdMission> swypeIdModel) {
        this.mRoot = cameraContainerView;

        this.activity = activity;
        this.mission = swypeIdModel.mission;
        AutoFitTextureView textureView = new AutoFitTextureView(mRoot.getContext());
        textureView.setMission(mission);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRoot.addView(textureView, lp);

        mission.camera.setTextureView(textureView);

        mission.video.onRecordingStart.add(this::onRecordingStart);
        mission.video.onRecordingStop.add(this::onRecordingStop);
    }

    private void onRecordingStart(CameraRecordConfig config) {
        mRoot.setKeepScreenOn(true);
        screenOrientationLock.lockScreenOrientation(activity);
    }

    private void onRecordingStop(File file, boolean isVideoConfirmed) {
        mRoot.setKeepScreenOn(false);
        screenOrientationLock.unlockScreen(activity);
    }
}
