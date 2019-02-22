package io.prover.swypeid.camera2;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.prover.common.util.Arrays;
import io.prover.swypeidlib.Const;

/**
 * Created by babay on 17.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2PrefsHelper {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    private static final int[] PREFERRED_FORMATS = {
            ImageFormat.YV12,
            ImageFormat.YUV_420_888,
            ImageFormat.NV21,
    };

    private static <T> boolean arrayContains(T[] array, T needle) {
        for (T value : array) {
            if (needle.equals(value))
                return true;
        }
        return false;
    }

    private static List<android.util.Size> selectSizes(android.util.Size[] choices, int min, int max, @Nullable Orientation orientation) {
        List<android.util.Size> result = new ArrayList<>();
        for (android.util.Size option : choices) {
            int width = option.getWidth();
            int height = option.getHeight();

            if (width >= min && width <= max && height >= min && height <= max) {
                if (orientation == null || orientation == Orientation.ofFrame(width, height))
                    result.add(option);
            }
        }
        return result;
    }

    private static List<Size> filterByOrientation(List<Size> sizes, Orientation orientation) {
        List<Size> result = new ArrayList<>();
        for (Size size : sizes) {
            if (size.getOrientation() == orientation)
                result.add(size);
        }
        return result;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param surfaceSize The minimum desired width
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public Size chooseOptimalSize(android.util.Size[] choices, Size surfaceSize, Size aspectRatio, float maxAspectDeviation) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        Size surfaceSize2 = surfaceSize.toOrientation(aspectRatio.getOrientation());
        List<android.util.Size> bigEnough = new ArrayList<>();
        for (android.util.Size option : choices) {
            if (option.getWidth() >= surfaceSize2.width && option.getHeight() >= surfaceSize2.height) {
                float aspect = option.getWidth() / (float) option.getHeight();
                if (Math.abs(aspect - aspectRatio.ratio) < maxAspectDeviation) {
                    bigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            android.util.Size size = Collections.min(bigEnough, new Camera2Util.CompareSizesByArea());
            return new Size(size);
        } else {
            Log.e(Const.TAG, "Couldn't find any suitable preview size");
            return new Size(choices[0]);
        }
    }

    public Size chooseOptimalDetectorSize(android.util.Size[] choices, int min, int max, Size videoSize) {
        List<android.util.Size> bigEnough = selectSizes(choices, min, max, videoSize.getOrientation());
        if (bigEnough.size() == 0) {
            bigEnough = selectSizes(choices, min, max, null);
        }

        if (bigEnough.size() > 0) {
            if (bigEnough.size() != 1) {
                Collections.sort(bigEnough, new Camera2Util.CompareSizesByArea());
                for (android.util.Size size : bigEnough) {
                    if (size.getWidth() > 140 && size.getHeight() > 140)
                        return new Size(size);
                }
            }
            return new Size(bigEnough.get(0));
        } else {
            Log.e(Const.TAG, "Couldn't find any suitable preview size");
            return new Size(choices[choices.length - 1]);
        }
    }

    public Size chooseOptimalDetectorSize(List<Size> suitableSizes, Size videoSize) {
        List<Size> orientedSizes = filterByOrientation(suitableSizes, videoSize.getOrientation());
        if (orientedSizes.size() > 0)
            return orientedSizes.get(0);

        return suitableSizes.get(0);
    }

    public List<Size> filterCaptureResolutionsForDetector(android.util.Size[] choices, int min, int max) {
        List<android.util.Size> suitableSizes = selectSizes(choices, min, max, null);
        Collections.sort(suitableSizes, new Camera2Util.CompareSizesByArea());
        if (suitableSizes.size() > 0)
            return Size.fromCamera2SizeList(suitableSizes);
        else {
            Log.e(Const.TAG, "Couldn't find any suitable preview size");
            java.util.Arrays.sort(choices, new Camera2Util.CompareSizesByArea());
            List<Size> result = new ArrayList<>();
            result.add(new Size(choices[0]));
            if (choices.length > 1)
                result.add(new Size(choices[1]));
            return result;
        }
    }

    public String selectCamera(@NonNull CameraManager manager, boolean isFront) throws CameraAccessException {
        String[] idList = manager.getCameraIdList();
        for (String cameraId : idList) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (isFront && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId;
            }
            if (!isFront && facing == CameraCharacteristics.LENS_FACING_BACK)
                return cameraId;
        }
        return idList[0];
    }

    public String selectNextCameraId(@Nullable String currentCameraId, @NonNull CameraManager manager) throws CameraAccessException {
        if (currentCameraId == null)
            return selectCamera(manager, false);

        String[] idList = manager.getCameraIdList();
        for (int i = 0; i < idList.length; i++) {
            String cameraId = idList[i];
            if (currentCameraId.equals(cameraId)) {
                return i == idList.length - 1 ? idList[0] : idList[i + 1];
            }
        }
        return idList[0];
    }

    public List<Size> loadCameraResolutions(StreamConfigurationMap map) {
        android.util.Size[] videoSizes = map.getOutputSizes(MediaRecorder.class);

        List<Size> result = new ArrayList<>();

        for (android.util.Size size : videoSizes) {
            int sWidth = size.getWidth();
            int sHeight = size.getHeight();
            if (sWidth >= sHeight && (sWidth > MAX_WIDTH || sHeight > MAX_HEIGHT))
                continue;
            if (sWidth < sHeight && (sWidth > MAX_HEIGHT || sHeight > MAX_WIDTH))
                continue;

            /*if (!arrayContains(imageSizes, size))
                continue;*/
            result.add(new Size(size));
        }

        Collections.sort(result, new Size.CameraAreaComparator());

        return result;
    }

    public int selectFormat(int[] outFormats) {
        for (int format : PREFERRED_FORMATS) {
            if (Arrays.contains(outFormats, format))
                return format;
        }

        return ImageFormat.YUV_420_888;
    }
}
