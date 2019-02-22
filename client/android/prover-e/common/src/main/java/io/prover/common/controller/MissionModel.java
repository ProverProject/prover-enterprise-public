package io.prover.common.controller;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.prover.common.transport.TransportModel;
import io.prover.common.transport.base.NetworkRequest;

/**
 * This abstract class is for implementing primary task: generate QR-code,
 * do a swype-confirmed video.
 */
public abstract class MissionModel
        implements TransportModel.OrderConfirmedListener, TransportModel.OrderRequestFailedListener {

    public final TransportModel transport;
    @NonNull
    public final Logger logger;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    public final ListenerList2Int<ControllerStateChangedListener> onControllerStateListener
            = new ListenerList2Int<>(mainHandler, ControllerStateChangedListener::onControllerStateChanged);
    public final ListenerList2<RootModel.GeneralErrorListener, Exception, NetworkRequest> generalErrorListener
            = new ListenerList2<>(mainHandler, RootModel.GeneralErrorListener::onControllerException);
    protected int state;
    @RootModel.ActivityState
    protected int activityState;

    @MainThread
    public MissionModel(TransportModel transport, @NonNull Logger logger) {
        this.transport = transport;
        this.logger = logger;
        transport.onOrderConfirmed.add(this);
        transport.onOrderRequestFailed.add(this);
    }

    @MainThread
    public void onChangeActivityState(@RootModel.ActivityState int state, @RootModel.ActivityState int oldState, Activity activity) {
        this.activityState = state;
        switch (state) {
            case RootModel.ActivityState.RESUMED:
                if (this.state == MissionStateBase.NOT_READY)
                    setState(MissionStateBase.READY);
                break;

            case RootModel.ActivityState.STARTED:
                if (this.state != MissionStateBase.NOT_READY) {
                    setState(MissionStateBase.NOT_READY);
                }
                break;

            case RootModel.ActivityState.DESTROYED:
                setState(MissionStateBase.NOT_READY);
                break;
        }
    }

    @MainThread
    protected final void setState(int state) {
        if (this.state != state) {
            int oldState = this.state;
            this.state = state;
            onChangeState(oldState, state);
            onControllerStateListener.postNotifyEvent(oldState, state);
        }
    }

    @MainThread
    protected void onChangeState(int oldState, int newState) {
        switch (newState) {
            case MissionStateBase.NOT_READY:
                transport.setBeReadyToWork(false);
                break;

            case MissionStateBase.READY:
                transport.setBeReadyToWork(true);
                break;
        }
    }

    public boolean canStartMainTask() {
        if (transport.isReadyForMainTask()) {
            return true;
        } else {
            transport.setBeReadyToStart(true);
            return false;
        }
    }

    @MainThread
    public void onFinishedMainTask() {
        setState(activityState == RootModel.ActivityState.RESUMED ? MissionStateBase.READY : MissionStateBase.NOT_READY);
    }

    public Logger getLogger() {
        return logger;
    }

    public void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread().equals(mainHandler.getLooper().getThread()))
            runnable.run();
        else mainHandler.post(runnable);
    }

    @MainThread
    public interface ControllerStateChangedListener {
        void onControllerStateChanged(int oldState, int newState);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MissionStateBase {
        int NOT_READY = 0;
        int READY = 1;
    }
}
