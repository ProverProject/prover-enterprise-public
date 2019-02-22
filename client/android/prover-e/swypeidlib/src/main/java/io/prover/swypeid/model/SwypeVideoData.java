package io.prover.swypeid.model;

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;

import java.io.File;

import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.swypeid.camera2.Size;

public class SwypeVideoData {
    final CameraRecordConfig recordConfig;
    final long videoStartTime;
    SwypeCode swypeCode;
    SwypeCode actualSwypeCode;
    volatile int swypeVersion;
    volatile ISwypeCodeOrderResult swypeCodeOrder;
    boolean isVideoConfirmed;
    File file;
    byte[] fileDigest;
    private boolean sendHashNoSwype;

    public SwypeVideoData(CameraRecordConfig recordConfig) {
        this.videoStartTime = System.currentTimeMillis();
        this.recordConfig = recordConfig;
    }

    public Size getDetectorSize() {
        return recordConfig.detectorSize;
    }

    public SwypeCode getSwypeCode() {
        return swypeCode;
    }

    public int getSwypeVersion() {
        return swypeVersion;
    }

    public void setSwypeCodeOrder(@NonNull ISwypeCodeOrderResult swypeCodeOrder) {
        if (swypeCodeOrder.getSwypeCode() != null) {
            this.swypeCodeOrder = swypeCodeOrder;
            this.swypeCode = new SwypeCode(swypeCodeOrder.getSwypeCode());
            this.actualSwypeCode = this.swypeCode.rotate(recordConfig.orientationHint);
        }
    }

    @AnyThread
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        this.isVideoConfirmed = isVideoConfirmed;
        this.file = file;
    }

    public boolean canSend() {
        return fileDigest != null && (swypeCodeOrder == null || !swypeCodeOrder.isFake());
    }

    public void setPostHashNoSwype(boolean sendHashNoSwype) {
        this.sendHashNoSwype = sendHashNoSwype;
    }

    public boolean isSendHashNoSwype() {
        return sendHashNoSwype;
    }
}
