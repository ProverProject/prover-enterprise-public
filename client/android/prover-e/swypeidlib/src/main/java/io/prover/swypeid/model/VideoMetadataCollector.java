package io.prover.swypeid.model;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.detector.DetectionStateChange;
import io.prover.swypeid.util.StatCounter;
import io.prover.swypeidlib.Const;

public class VideoMetadataCollector implements SwypeIdDetector.OnDetectionStateCahngedListener {
    private final SwypeIdMission mission;

    private final List<DetectionStateChange> stateChanges = new ArrayList<>();
    //private final IntKeeper debugKeeper = new IntKeeper();

    private final StatCounter detectionTimesStats = new StatCounter();


    VideoMetadataCollector(SwypeIdMission mission) {
        this.mission = mission;
        mission.detector.detectionState.add(this);
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    @Override
    public void onDetectionStateChanged(@NonNull DetectionStateChange stateChange) {
        switch (stateChange.event) {
            case StartSwypeCode:
            case NextSwypeCodeIndex:
            case CompletedSwypeCode:
                stateChanges.add(stateChange);
                break;

            case FailedSwypeCode:
            case StartDetection:
                stateChanges.clear();
                break;
        }

        if (stateChange.timeToAnalyze > 2) {
            detectionTimesStats.add(stateChange.timeToAnalyze);
        }
        //debugKeeper.add(stateChange.state.d);
    }

    public void clear() {
        stateChanges.clear();
        detectionTimesStats.clear();
    }

    public Map<String, String> getMetadata(SwypeVideoData videoData) {
        Map<String, String> meta = new HashMap<>();
        meta.put("prover", getProverMetadataString(videoData));
        meta.put("title", "Prover video");
        meta.put("proverStats", getStatsMetadataString());

        Log.d(Const.TAG, String.format("VideoMetadataCollector average detect time: %f, min: %d, max: %d",
                detectionTimesStats.avg(), detectionTimesStats.min(), detectionTimesStats.max()));
        return meta;
    }

    private String getProverMetadataString(SwypeVideoData videoData) {
        StringBuilder builder = new StringBuilder();
        Size size = videoData.getDetectorSize();
        builder.append("size:")
                .append(size.width)
                .append(",")
                .append(size.height)
                .append(";code:");
        if (mission.detector.isVideoConfirmed()) {
            builder.append(videoData.getSwypeCode().getCodeV2());
        }
        builder.append(";");
        for (int i = 0; i < stateChanges.size(); i++) {
            if (i > 0)
                builder.append(",");
            builder.append(stateChanges.get(i).state.timestamp);
        }
        builder.append(";version:").append(videoData.getSwypeVersion()).append(";");
        return builder.toString();
    }

    private String getStatsMetadataString() {
        StringBuilder builder = new StringBuilder();

        builder.append("device:").append(getDeviceName()).append(";")
                .append(String.format(Locale.US, "detectionTime:%d,%d,%.2f;",
                        detectionTimesStats.min(), detectionTimesStats.max(), detectionTimesStats.avg()));

        List<Size> sizes = mission.video.getAvailableCaptureSizes();
        builder.append("sizes:");
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            if (size.width >= 100 && size.width <= 320 && size.height >= 100 && size.height <= 320) {
                builder.append(size.width).append("x").append(size.height).append(",");
            }
        }
        builder.replace(builder.length() - 1, builder.length(), ";");
        return builder.toString();
    }
}