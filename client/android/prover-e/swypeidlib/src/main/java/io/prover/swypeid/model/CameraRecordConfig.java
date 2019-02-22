package io.prover.swypeid.model;

import io.prover.swypeid.camera2.Camera2Config;
import io.prover.swypeid.camera2.OrientationHelper;
import io.prover.swypeid.camera2.Size;

public class CameraRecordConfig {
    public final int orientationHint;
    public final int rotation;
    @OrientationHelper.SensorOrientation
    public final int sensorOrientation;
    public Size videoSize;
    public Size detectorSize;

    public CameraRecordConfig(Camera2Config config, int rotation) {
        this.orientationHint = OrientationHelper.getOrientationHint(config.sensorOrientation, rotation);
        this.rotation = rotation;
        this.videoSize = config.videoSize;
        this.detectorSize = config.captureFrameSize;
        this.sensorOrientation = config.sensorOrientation;
    }
}
