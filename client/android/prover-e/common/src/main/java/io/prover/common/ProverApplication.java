package io.prover.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatDelegate;

import java.lang.ref.WeakReference;

public class ProverApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static String PACKAGE_NAME;
    private static WeakReference<ProverApplication> appRef;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private int startedActivities;
    private int resumedActivities;
    private int createdActivities;
    private int mainActivitiesStarted;
    private int mainActivitiesResumed;

    public static ProverApplication getApp() {
        return appRef == null ? null : appRef.get();
    }

    public static String getAppPackageName() {
        return PACKAGE_NAME;
    }

    @Override
    public void onCreate() {
        appRef = new WeakReference<>(this);
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        PACKAGE_NAME = getPackageName();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ++createdActivities;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++startedActivities;
        if (activity instanceof IMainActivity)
            ++mainActivitiesStarted;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumedActivities;
        if (activity instanceof IMainActivity) {
            ++mainActivitiesResumed;
            //if (SessionManager.INSTANCE.isSessionValid()) {
            ((IMainActivity) activity).getController().onResume(activity);
            //}
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        --resumedActivities;
        if (activity instanceof IMainActivity) {
            --mainActivitiesResumed;
            ((IMainActivity) activity).getController().onPause(activity);
        }
    }

    @Override
    @UiThread
    public void onActivityStopped(Activity activity) {
        --startedActivities;
        if (activity instanceof IMainActivity) {
            --mainActivitiesStarted;
            ((IMainActivity) activity).getController().onStop(activity, mainActivitiesStarted > 0);
        }
        if (startedActivities < 0)
            startedActivities = 0;
        if (mainActivitiesStarted < 0)
            mainActivitiesStarted = 0;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        --createdActivities;
        if (activity instanceof IMainActivity) {
            ((IMainActivity) activity).getController().onActivityDestroy(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    public int getStartedActivities() {
        return startedActivities;
    }

    public int getResumedActivities() {
        return resumedActivities;
    }

    public int getCreatedActivities() {
        return createdActivities;
    }

    public boolean isMainActivityActive() {
        return mainActivitiesStarted + mainActivitiesResumed > 0;
    }
}
