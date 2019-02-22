package io.prover.swypeid.model;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import org.mp4parser.MetaDataInsert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;
import java.util.Map;

import io.prover.common.util.UtilFile;
import io.prover.swypeid.camera2.CameraUtil;
import io.prover.swypeidlib.Settings;

import static io.prover.common.Const.TAG;

class VideoPostRecordingProcessor {
    @NonNull
    final SwypeVideoData videoData;
    private final Context context;
    private final OnDonePostProcessingCallback callback;
    private final Map<String, String> metadata;
    private final Handler handler = new Handler(Looper.getMainLooper());

    VideoPostRecordingProcessor(Context context, OnDonePostProcessingCallback callback, @NonNull SwypeVideoData videoData, Map<String, String> metadata) {
        this.context = context;
        this.callback = callback;
        this.metadata = metadata;
        this.videoData = videoData;
    }

    VideoPostRecordingProcessor execute() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this::run);
        return this;
    }

    public void run() {
        long fileSize1 = videoData.file.length();
        long fileSize2 = 0;
        long fileSize3 = 0;
        Exception exception = null;
        try {
            videoData.file = renameFile(videoData.file);
            fileSize2 = videoData.file.length();
            updateMetadata(videoData.file);
            fileSize3 = videoData.file.length();
            videoData.fileDigest = calculateFileHash(videoData.file);
        } catch (Exception e) {
            exception = e;
            Log.e(TAG, "run: ", e);
        }
        if (videoData.file.length() == 0) {
            String err = String.format(Locale.US, "Error: zero file size: s1: %d, s2: %d, s3: %d", fileSize1, fileSize2, fileSize3);
            Log.e(TAG, "run: zero file sizes", new RuntimeException(err));
        }

        String path = videoData.file.getAbsolutePath();
        final Exception ex = exception;
        handler.postDelayed(() -> MediaScannerConnection.scanFile(context, new String[]{path}, null, null), 1_000);
        handler.post(() -> callback.onDoneVideoPostProcessing(this, ex));

    }

    private File renameFile(File file) {
        File outputFile = CameraUtil.getOutputMediaFile(CameraUtil.MEDIA_TYPE_VIDEO, context, videoData.videoStartTime);
        if (outputFile == null)
            outputFile = file;
        if (Settings.ADD_SWYPE_CODE_TO_FILE_NAME && videoData.isVideoConfirmed && videoData.swypeCode != null) {
            outputFile = UtilFile.addFileNameSuffix(outputFile, "_" + videoData.swypeCode.getCodeV2().replace("*", "s"));
        }
        if (file.renameTo(outputFile)) {
            return outputFile;
        }
        return file;
    }

    private void updateMetadata(File file) {
        try {
            new MetaDataInsert()
                    .writeMetadata(file, metadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] calculateFileHash(File file) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        long timeStart = System.currentTimeMillis();
        long totalAmount = 0;
        MessageDigest digest = MessageDigest.getInstance("sha256", "BC");
        byte[] buf = new byte[4096];
        FileInputStream stream = new FileInputStream(file);
        while (stream.available() > 0) {
            int amount = stream.read(buf);
            totalAmount += amount;
            digest.update(buf, 0, amount);
        }
        Log.d(TAG, "calculateFileHash: took " + (System.currentTimeMillis() - timeStart) + "ms, amount: " + totalAmount);
        return digest.digest();
    }

    @UiThread
    public interface OnDonePostProcessingCallback {
        void onDoneVideoPostProcessing(VideoPostRecordingProcessor processor, Exception exception);
    }
}
