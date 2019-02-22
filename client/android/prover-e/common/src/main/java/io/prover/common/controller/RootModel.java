package io.prover.common.controller;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.prover.common.transport.TransportModel;
import io.prover.common.transport.base.NetworkRequest;

import static io.prover.common.controller.RootModel.ActivityState.DESTROYED;

@MainThread
public abstract class RootModel<T extends TransportModel, M extends MissionModel> {

    public final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList1Sync<ActivityResumeListener, Activity> onActivityResume
            = new ListenerList1Sync<>(ActivityResumeListener::onActivityResume);

    public final ListenerList2IntSync<ActivityStateChangedListener> onActivityStateListener
            = new ListenerList2IntSync<>(ActivityStateChangedListener::onActivityStateChanged);

    public final M mission;
    public final T transport;

    @RootModel.ActivityState
    protected int activityState;
    protected Activity ownerActivity;

    public RootModel(Activity activity, T transport, M mission) {
        this.transport = transport;
        this.mission = mission;

        mission.logger.setModelRoot(this);
        transport.onNetworkRequestErrorListener.add(this::onNetworkRequestError);

        setOwnerActivity(activity, ActivityState.DESTROYED);
    }

    public void onStop(Activity activity, boolean hasOtherRunningControllers) {
        if (this.ownerActivity == activity) {
            setActivityState(ActivityState.CREATED, activity);
        }

        if (!hasOtherRunningControllers && activity.isFinishing()) {
            mission.onChangeActivityState(DESTROYED, ActivityState.CREATED, activity);
        }
    }

    protected void onActivityStateChange(@ActivityState int oldState, @ActivityState int newState, Activity activity) {
        onActivityStateListener.notifyEvent(oldState, newState);
        if (newState != DESTROYED)
            mission.onChangeActivityState(newState, oldState, activity);
        switch (activityState) {
            case ActivityState.RESUMED:
                transport.onActivityResume();
                break;

            case ActivityState.STARTED:
                if (oldState == ActivityState.RESUMED) {
                    transport.onActivityPause();
                }
                break;

        }
    }

    private void onNetworkRequestError(NetworkRequest request, Exception e) {
        mission.generalErrorListener.postNotifyEvent(e, request);
    }

    public final void setOwnerActivity(Activity ownerActivity, @ActivityState int activityState) {
        this.ownerActivity = ownerActivity;
        setActivityState(activityState, ownerActivity);
    }

    protected final void setActivityState(@ActivityState int activityState, @NonNull Activity activity) {
        if (activityState != this.activityState) {
            int oldState = this.activityState;
            this.activityState = activityState;
            onActivityStateChange(oldState, activityState, activity);
        }
    }

    public void onResume(Activity activity) {
        if (this.ownerActivity == activity) {
            onActivityResume.notifyEvent(activity);
            setActivityState(ActivityState.RESUMED, activity);
        }
    }

    public void onPause(Activity activity) {
        if (this.ownerActivity == activity) {
            setActivityState(ActivityState.STARTED, activity);
        }
    }

    public void onActivityDestroy(Activity activity) {
        if (this.ownerActivity == activity) {
            setActivityState(ActivityState.DESTROYED, activity);
        }
    }

    @MainThread
    public interface ActivityResumeListener {
        void onActivityResume(Activity activity);
    }

    @MainThread
    public interface ActivityStateChangedListener {
        void onActivityStateChanged(int oldState, int newState);
    }

    @MainThread
    public interface GeneralErrorListener {
        /**
         * @param e              -- exception
         * @param networkRequest -- not null if it is network request error
         */
        void onControllerException(Exception e, NetworkRequest networkRequest);
    }

    @IntDef({
            DESTROYED,
            RootModel.ActivityState.CREATED,
            RootModel.ActivityState.STARTED,
            RootModel.ActivityState.RESUMED,
    })
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActivityState {
        int DESTROYED = 0;
        int CREATED = 1;
        int STARTED = 2;
        int RESUMED = 3;
    }
}