package io.prover.swypeid.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;

import io.prover.swypeid.camera2.Size;

public interface ISurfacesHolder {
    Size getPreviewSurfaceSize();

    void setPreviewSize(Size previewSize);

    void configurePreview(Activity activity, Size cameraPreviewSize);

    SurfaceTexture getPreviewSurfaceTexture();

    SurfaceTexture getRendererInputTexture();

    void onResume(Activity activity);

    boolean isAvailable();

    interface SurfacesHolderListener {
        void onSurfaceDestroyed();

        void onReady();
    }
}
