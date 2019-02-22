package io.prover.swypeid.model;

import android.support.annotation.NonNull;

import java.util.Locale;

import io.prover.common.controller.Logger;
import io.prover.common.transport.TransportModel;
import io.prover.common.view.ScreenLogger;
import io.prover.swypeid.detector.DetectionStateChange;

public class SwypeIdLogger extends Logger {

    SwypeIdLogger(TransportModel transportModel) {
        super(transportModel);
    }

    public void setMission(SwypeIdMission mission) {
        mission.detector.swypeCodeSet.add(this::onSwypeCodeSet);
        mission.detector.detectionState.add(this::onDetectionStateChanged);
        mission.video.onRecordingStart.add(this::onRecordingStart);
    }

    private void onSwypeCodeSet(SwypeCode swypeCode, SwypeCode actualSwypeCode, Integer orientationHint) {
        String msg = String.format("Swype code: %s, transformed: %s", swypeCode.getCodeV2(), actualSwypeCode.getCodeV2());
        addToScreenLog(msg, ScreenLogger.MessageType.GENERAL);
    }

    private void onDetectionStateChanged(@NonNull DetectionStateChange stateChange) {
        switch (stateChange.event) {
            case Nothing:
                break;

            case NextSwypeCodeIndex:
                addToScreenLog("Detector: NextSwypeCodeIndex: " + stateChange.state.index, ScreenLogger.MessageType.GENERAL);
                break;

            case StartDetection:
            case CircleDetected:
            case StartSwypeCode:
            case FailedSwypeCode:
            case CompletedSwypeCode:
                addToScreenLog("Detector: " + stateChange.event.name(), ScreenLogger.MessageType.GENERAL);
                break;
        }
    }

    private void onRecordingStart(CameraRecordConfig config) {
        String msg = String.format(Locale.US,
                "Start video %dx%d, detector %dx%d, sensor orientation: %d,  screen rotation: %d, orientationHint: %d",
                config.videoSize.width, config.videoSize.height, config.detectorSize.width, config.detectorSize.height,
                config.sensorOrientation, config.rotation, config.orientationHint);
        addToScreenLog(msg, ScreenLogger.MessageType.GENERAL);
    }
}
