package io.prover.swypeid.detector;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.model.SwypeCode;
import io.prover.swypeid.model.SwypeIdDetector;
import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.model.VideoRecorder;
import io.prover.swypeid.util.Frame;
import io.prover.swypeidlib.BuildConfig;
import io.prover.swypeidlib.Const;


/**
 * Created by babay on 11.11.2017.
 */

public class SwypeDetectorHandler extends Handler implements SwypeIdDetector.OnSwypeCodeSetListener,
        VideoRecorder.OnRecordingStopListener, SwypeIdDetector.SwypeCodeConfirmedListener {
    private static final int MAX_FRAMES_IN_QUEUE = 2;

    private static final int MESSAGE_INIT = 1;
    private static final int MESSAGE_SET_SWYPE = 2;
    private static final int MESSAGE_PROCESS_FRAME = 3;
    private static final int MESSAGE_QUIT = 4;
    private static volatile int counter = 0;
    private final HandlerThread handlerThread;

    private final ProverDetector detector;
    private final AtomicInteger framesInQueue = new AtomicInteger();
    private final SwypeIdMission mision;
    private final Size videoSize;
    private final Size detectorSize;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean processing = true;
    private boolean quitDone = false;
    private long videoStartTime = -1;

    private SwypeDetectorHandler(Looper looper, SwypeIdMission mision, Size videoSize, Size detectorSize) {
        super(looper);
        handlerThread = (HandlerThread) looper.getThread();
        detector = new ProverDetector(mision);
        this.mision = mision;
        this.videoSize = videoSize;
        this.detectorSize = detectorSize;
        mision.detector.swypeCodeSet.add(this);
        mision.video.onRecordingStop.add(this);
        mision.detector.swypeCodeConfirmed.add(this);
    }

    public static SwypeDetectorHandler newHandler(Size videoSize, Size detectorSize, SwypeIdMission mission) {
        HandlerThread handlerThread = new HandlerThread("SwypeDetectorThread_" + ++counter);
        handlerThread.start();
        SwypeDetectorHandler handler = new SwypeDetectorHandler(handlerThread.getLooper(), mission, videoSize, detectorSize);
        handler.sendInit();
        return handler;
    }

    private void sendInit() {
        sendMessage(obtainMessage(MESSAGE_INIT, 0, 0));
    }

    private void sendSetSwype(@NonNull SwypeCode swype) {
        sendMessage(obtainMessage(MESSAGE_SET_SWYPE, swype));
    }

    public void onFrameAvailable(Frame frame) {
        if (videoStartTime < 0)
            videoStartTime = System.currentTimeMillis();
        if (processing) {
            frame.setTimeStamp((int) (System.currentTimeMillis() - videoStartTime));
            int inQueue = framesInQueue.get();
            if (inQueue < MAX_FRAMES_IN_QUEUE && processing) {
                framesInQueue.incrementAndGet();
                sendMessage(obtainMessage(MESSAGE_PROCESS_FRAME, frame));
                return;
            } else {
                Log.e(Const.TAG, "frames processing queue maxed out!");
            }
        }
        frame.recycle();
    }

    public void sendQuit() {
        processing = false;
        sendMessage(obtainMessage(MESSAGE_QUIT));
    }

    public void quitSync() {
        processing = false;
        sendMessage(obtainMessage(MESSAGE_QUIT));
        synchronized (detector) {
            while (!quitDone) {
                try {
                    detector.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_INIT:
                if (processing) {
                    detector.init(videoSize, detectorSize);
                }
                return;

            case MESSAGE_SET_SWYPE:
                if (processing)
                    detector.setSwype((SwypeCode) msg.obj);
                return;

            case MESSAGE_PROCESS_FRAME:
                Frame frame = (Frame) msg.obj;
                if (processing) {
                    detector.detectFrame(frame);
                }
                framesInQueue.decrementAndGet();
                frame.recycle();
                break;

            case MESSAGE_QUIT:
                mision.detector.swypeCodeSet.remove(this);
                mision.video.onRecordingStop.remove(this);
                mision.detector.swypeCodeConfirmed.remove(this);
                detector.release();
                quitDone = true;
                synchronized (detector) {
                    try {
                        detector.notifyAll();
                    } catch (Exception ignored) {
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    handlerThread.quitSafely();
                } else {
                    handlerThread.quit();
                }
                return;
        }
        super.handleMessage(msg);
    }

    @Override
    public void onSwypeCodeSet(SwypeCode swypeCode, SwypeCode actualSwypeCode, Integer orientationHint) {
        if (actualSwypeCode != null)
            sendSetSwype(actualSwypeCode);
        else {
            if (BuildConfig.DEBUG)
                throw new NullPointerException("actualSwypeCode is null");
            Log.e(Const.TAG, "onSwypeCodeSet: ", new NullPointerException("actualSwypeCode is null"));
        }
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        sendQuit();
    }

    @Override
    public void onSwypeCodeConfirmed() {
        handler.postDelayed(this::sendQuit, 1000);
    }

    public boolean isAlive() {
        return processing;
    }
}
