package io.prover.clapperboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.prover.common.BuildConfig;
import io.prover.common.Const;
import io.prover.common.controller.Logger;
import io.prover.common.controller.MissionModel;
import io.prover.common.controller.RootModel;
import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.transport.OrderType;
import io.prover.common.transport.TransportModel;

import static io.prover.common.Const.ARG_ORDER_REQUEST;
import static io.prover.common.Const.TAG;

@MainThread
public class ClapperboardMissionModel extends MissionModel {

    private QrCodeOrderData qrCodeOrderResult;

    @MainThread
    public ClapperboardMissionModel(TransportModel transport, Activity activity) {
        super(transport, new Logger(transport));

//        if (!restoreState(activity))
        //setState(ClapperboardMissionState.READY);

        transport.onQrCodeOrderComplete.add(this::onQrCodeOrderComplete);
    }

    @MainThread
    private void onQrCodeOrderComplete(IQrCodeOrderResult result, long timeSpent) {
        if (result == null) {
            onFinishedMainTask();
        } else {
            qrCodeOrderResult = new QrCodeOrderData(result);
            setState(ClapperboardMissionState.HAVE_QR_CODE);
        }
    }

    @MainThread
    public void requestQrCode(String message) {
        setState(ClapperboardMissionState.REQUESTING_QR_CODE);
        transport.requestQrCode(message);
    }

    @Override
    @MainThread
    public void onOrderConfirmed(@NonNull OrderType orderType) {

    }

    @Override
    @MainThread
    public void onOrderRequestFailed(@NonNull OrderType orderType, Exception e) {
        onFinishedMainTask();
    }

    @ClapperboardMissionState
    @MainThread
    public int getState() {
        return state;
    }

    @Override
    @MainThread
    protected void onChangeState(@ClapperboardMissionState int oldState, @ClapperboardMissionState int newState) {
        super.onChangeState(oldState, newState);
        switch (newState) {
            case ClapperboardMissionState.NOT_READY:
            case ClapperboardMissionState.READY:
                qrCodeOrderResult = null;
                break;

            case ClapperboardMissionState.REQUESTING_QR_CODE:
                transport.setBeReadyToStart(false);
                break;

            case ClapperboardMissionState.HAVE_QR_CODE:
                break;
        }
    }

    @Override
    @MainThread
    public void onChangeActivityState(@RootModel.ActivityState int newState, @RootModel.ActivityState int oldState, Activity activity) {
        if (oldState == RootModel.ActivityState.RESUMED && newState == RootModel.ActivityState.STARTED) {
            persistState(activity);
        }
        super.onChangeActivityState(newState, oldState, activity);
        if (oldState != RootModel.ActivityState.RESUMED && newState == RootModel.ActivityState.RESUMED) {
            restoreState(activity);
        }
    }

    @MainThread
    private void persistState(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            if (qrCodeOrderResult != null)
                prefs.edit().putString(Const.ARG_ORDER_RESULT, qrCodeOrderResult.toJson().toString()).apply();
            else transport.persistTask(prefs);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                throw new RuntimeException(e);
            else
                Log.e(TAG, "persistState: ", e);
        }
    }

    @MainThread
    public void clearStoredState(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        boolean changed = false;
        if (prefs.contains(Const.ARG_ORDER_RESULT)) {
            editor.remove(Const.ARG_ORDER_RESULT);
            changed = true;
        }
        if (prefs.contains(ARG_ORDER_REQUEST)) {
            editor.remove(ARG_ORDER_REQUEST);
            changed = true;
        }
        if (changed)
            editor.apply();
    }

    @MainThread
    public IQrCodeOrderResult getQrCodeOrder() {
        return qrCodeOrderResult;
    }

    /**
     * restores state
     *
     * @return true if actually restored some state
     */
    @MainThread
    private boolean restoreState(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (prefs.contains(Const.ARG_ORDER_RESULT)) {
            try {
                qrCodeOrderResult = new QrCodeOrderData(new JSONObject(prefs.getString(Const.ARG_ORDER_RESULT, null)));
                qrCodeOrderResult.getQrCodeBytes();
                setState(ClapperboardMissionState.HAVE_QR_CODE);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "restoreState: ", e);
                prefs.edit().remove(Const.ARG_ORDER_RESULT).apply();
            }
        }
        if (transport.resumeTask(prefs)) {
            setState(ClapperboardMissionState.REQUESTING_QR_CODE);
            return true;
        }
        return false;
    }


    @IntDef({
            ClapperboardMissionState.NOT_READY,
            ClapperboardMissionState.READY,
            ClapperboardMissionState.REQUESTING_QR_CODE,
            ClapperboardMissionState.HAVE_QR_CODE
    })
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClapperboardMissionState {
        int NOT_READY = MissionStateBase.NOT_READY;
        int READY = MissionStateBase.READY;
        int REQUESTING_QR_CODE = 2;
        int HAVE_QR_CODE = 3;
    }
}
