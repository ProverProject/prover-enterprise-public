package io.prover.common.transport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import static io.prover.common.Const.TAG;

public class RequestAmountChangesHandler {
    private static final long MIN_TIME_BETWEEN_STATUS_CHANGES = 800;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final TransportModel.RunningRequestsChangedListener callback;

    private int latestRequestsAmount = 0;
    private boolean latestRequestWasWithError = false;
    private long latestStatusChangeTime = 0;
    private boolean updateScheduled = false;

    RequestAmountChangesHandler(TransportModel.RunningRequestsChangedListener callback) {
        this.callback = callback;
    }

    public void onRequestAmountChanged(int amount) {
        if (amount != 0)
            latestRequestWasWithError = false;
        latestRequestsAmount = amount;
        checkNotify();
    }

    public void onNetworkRequestGotError() {
        latestRequestWasWithError = true;
    }

    private void checkNotify() {
        if (!updateScheduled) {
            long timePassed = System.currentTimeMillis() - latestStatusChangeTime;
            if (timePassed > MIN_TIME_BETWEEN_STATUS_CHANGES) {
                dispatchEvent();
            } else {
                updateScheduled = true;
                handler.postDelayed(this::dispatchEvent, MIN_TIME_BETWEEN_STATUS_CHANGES - timePassed);
            }
        }
    }

    private void dispatchEvent() {
        updateScheduled = false;
        latestStatusChangeTime = System.currentTimeMillis();
        callback.onRunningRequestsAmountChanged(latestRequestsAmount, latestRequestWasWithError);
        Log.d(TAG, "RequestAmountChangesHandler::dispatchEvent:");
    }
}
