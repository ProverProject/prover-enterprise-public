package io.prover.swypeid.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;

import java.util.Collections;
import java.util.List;

/**
 * Created by babay on 12.12.2017.
 */

public class Camera2Config {

    private static final int MIN_CAPTURE_SIZE = 100;
    public final List<Size> cameraResolutions;
    public final List<Size> captureResolutions;
    public final int mImageFormat;
    @OrientationHelper.SensorOrientation
    public final int sensorOrientation;
    public final int[] availableCaptureFormats;
    public final boolean isFrontCamera;
    private final Camera2PrefsHelper camera2PrefsHelper = new Camera2PrefsHelper();
    private final ResolutionSelector resolutionSelector = new ResolutionSelector();
    public Size videoSize;
    public Size previewSize;
    public Size captureFrameSize;
    private String mCameraId;

    public Camera2Config(CameraManager manager, String cameraId) throws CameraAccessException {
        this.mCameraId = cameraId;

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new RuntimeException("Cannot get available preview/video sizes");
        }

        availableCaptureFormats = map.getOutputFormats();
        mImageFormat = camera2PrefsHelper.selectFormat(availableCaptureFormats);
        cameraResolutions = Collections.unmodifiableList(camera2PrefsHelper.loadCameraResolutions(map));
        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        android.util.Size[] availableCaptureSizes = map.getOutputSizes(mImageFormat);
        captureResolutions = Collections.unmodifiableList(camera2PrefsHelper.filterCaptureResolutionsForDetector(availableCaptureSizes, MIN_CAPTURE_SIZE, 320));

        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        isFrontCamera = CameraCharacteristics.LENS_FACING_FRONT == facing;
    }

    public void selectResolutions(Size surfaceSize, Size selectedSize, Context context) throws CameraAccessException {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        surfaceSize = surfaceSize.scale(0.5f, 0.5f);

        videoSize = resolutionSelector.selectResolution(selectedSize, cameraResolutions, surfaceSize, context);
        captureFrameSize = camera2PrefsHelper.chooseOptimalDetectorSize(captureResolutions, videoSize);
        previewSize = camera2PrefsHelper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceSize, videoSize, 0.1f);
    }

    public ImageReader imageReader(int maxImages) {
        return ImageReader.newInstance(captureFrameSize.width, captureFrameSize.height, mImageFormat, maxImages);
    }

    public String getCameraId() {
        return mCameraId;
    }
}
