package io.prover.swypeid.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.TextureView;

import io.prover.swypeid.camera2.AutoFitTextureView;
import io.prover.swypeid.camera2.OrientationHelper;
import io.prover.swypeid.camera2.Size;

/**
 * Created by babay on 24.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SimpleSurfacesHolder implements ISurfacesHolder {

    private static final Object sync = new Object();
    private final AutoFitTextureView textureView;
    private final SurfacesHolderListener listener;
    private final Activity activity;
    private volatile boolean screenTextureReady;
    private Size cameraPreviewSize;
    private Size mSurfaceSize;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            onScreenTextureReady(width, height);
        }


        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            if (cameraPreviewSize != null && activity != null) {
                textureView.configureTransform(activity, cameraPreviewSize, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            synchronized (sync) {
                screenTextureReady = false;
            }
            listener.onSurfaceDestroyed();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };
    private int rotationAngle;

    public SimpleSurfacesHolder(Activity activity, AutoFitTextureView textureView, SurfacesHolderListener listener) {
        this.textureView = textureView;
        this.activity = activity;
        this.listener = listener;
    }

    private void onTextureReady() {
        if (screenTextureReady) {
            listener.onReady();
        }
    }

    public void onResume(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        rotationAngle = OrientationHelper.getRotationAngle(rotation);
        screenTextureReady = false;

        if (textureView.isAvailable()) {
            onScreenTextureReady(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void onScreenTextureReady(int width, int height) {
        mSurfaceSize = new Size(width, height);
        synchronized (sync) {
            screenTextureReady = true;
        }
        onTextureReady();
    }

    public Size getPreviewSurfaceSize() {
        return mSurfaceSize;
    }

    public void setPreviewSize(Size previewSize) {
        cameraPreviewSize = previewSize;
    }

    @Override
    public SurfaceTexture getRendererInputTexture() {
        return null;
    }

    @Override
    public void configurePreview(Activity activity, Size cameraPreviewSize) {
        textureView.configurePreviewSize(cameraPreviewSize);
        textureView.configureTransform(activity, cameraPreviewSize, textureView.getWidth(), textureView.getHeight());
    }

    @Override
    public SurfaceTexture getPreviewSurfaceTexture() {
        return textureView.getSurfaceTexture();
    }

    @Override
    public boolean isAvailable() {
        return textureView.isAvailable();
    }


}
