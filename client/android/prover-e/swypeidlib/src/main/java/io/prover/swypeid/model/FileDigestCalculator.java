package io.prover.swypeid.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static io.prover.common.Const.TAG;

public class FileDigestCalculator {

    public final File file;
    private final Handler handler;
    private final FileHashCalculatorListener listener;


    public FileDigestCalculator(File file, Handler handler, FileHashCalculatorListener listener) {
        this.file = file;
        this.handler = handler;
        this.listener = listener;
    }

    public FileDigestCalculator execute() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this::run);
        return this;
    }

    public void run() {
        Exception exception = null;
        byte[] digest = null;
        try {
            digest = calculateFileHash();
        } catch (NoSuchAlgorithmException | IOException | NoSuchProviderException e) {
            Log.e(TAG, "calculateFileHash: ", e);
            exception = e;
        }
        final byte[] di = digest;
        final Exception ex = exception;
        handler.post(() -> listener.onFileDigestCalculated(di, ex));
    }

    private byte[] calculateFileHash() throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
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

    public interface FileHashCalculatorListener {
        void onFileDigestCalculated(byte[] digest, Exception e);
    }
}
