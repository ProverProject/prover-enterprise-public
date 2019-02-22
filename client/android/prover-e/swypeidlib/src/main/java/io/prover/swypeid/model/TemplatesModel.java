package io.prover.swypeid.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

import io.prover.common.controller.ListenerList1;
import io.prover.common.controller.ListenerList1Sync;
import io.prover.common.controller.ListenerList2;
import io.prover.swypeid.camera2.Camera2Config;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.templates.Template;

public class TemplatesModel {
    public final ListenerList1Sync<OnTemplatePagerVisibilityChangeListener, Boolean> onTemplatePagerVisibilityChange =
            new ListenerList1Sync<>(OnTemplatePagerVisibilityChangeListener::onTemplatePagerVisibilityChange);
    private final Handler handler = new Handler(Looper.getMainLooper());
    public final ListenerList2<OnTemplatePageChangeListener, Integer, Integer> onTemplatePageChange
            = new ListenerList2<>(handler, OnTemplatePageChangeListener::onTemplatePageChanged);
    public final ListenerList1<OnTemplateSetListener, Template> onTemplateSet =
            new ListenerList1<>(handler, OnTemplateSetListener::onTemplateSet);

    private final Context context;
    private final SwypeIdMission mission;

    private Template template;

    private boolean templatePagerVisible;

    public TemplatesModel(SwypeIdMission mission, Context context) {
        this.mission = mission;
        this.context = context;

        mission.video.previewStart.add(this::onPreviewStart);
        mission.video.onRecordingStop.add(this::onRecordingStop);
    }

    @MainThread
    private void onRecordingStop(File file, boolean isVideoConfirmed) {
        setTemplate(null);
    }

    @MainThread
    private void onPreviewStart(List<Size> sizes, Size size) {
        if (template != null) {
            mission.startRecording();
            setTemplatePagerVisible(template != null);
        }
    }

    public Template getTemplate() {
        return template;
    }

    @MainThread
    public void setTemplate(@Nullable Template template) {
        this.template = template;
        onTemplateSet.postNotifyEvent(template);

        if (template != null) {
            Camera2Config config = mission.camera.getCameraConfig();
            if (config == null || !mission.camera.isCameraOpen()) {
                mission.camera.selectCameraAndOpen(template.cameraPreference == Template.CameraPreference.FRONT);
            } else if (template.cameraPreference == Template.CameraPreference.BACK && config.isFrontCamera) {
                mission.camera.selectCameraAndOpen(false);
            } else if (template.cameraPreference == Template.CameraPreference.FRONT && !config.isFrontCamera) {
                mission.camera.selectCameraAndOpen(true);
            } else {
                mission.startRecording();
                setTemplatePagerVisible(true);
            }
        } else {
            setTemplatePagerVisible(false);
        }
    }

    public boolean isTemplatePagerVisible() {
        return templatePagerVisible;
    }

    @MainThread
    public void setTemplatePagerVisible(boolean templatePagerVisible) {
        this.templatePagerVisible = templatePagerVisible;
        onTemplatePagerVisibilityChange.notifyEvent(templatePagerVisible);
    }

    public interface OnTemplateSetListener {
        void onTemplateSet(@Nullable Template template);
    }

    public interface OnTemplatePagerVisibilityChangeListener {
        void onTemplatePagerVisibilityChange(boolean visible);
    }

    public interface OnTemplatePageChangeListener {
        void onTemplatePageChanged(int current, int total);
    }
}
