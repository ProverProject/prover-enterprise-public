package io.prover.swypeid.util;

import android.util.Log;

import static io.prover.common.Const.TAG;

public class TaskSyncWaiter {
    private boolean done;

    public void waitForDone() {
        synchronized (this) {
            while (!done) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void waitForDone(long maxTimeToWait) {
        long endTimestamp = System.currentTimeMillis() + maxTimeToWait;
        synchronized (this) {
            while (!done) {
                try {
                    this.wait(maxTimeToWait + 1);
                } catch (InterruptedException ignored) {

                }
                if (System.currentTimeMillis() >= endTimestamp) {
                    Log.d(TAG, "timed out waitForDone: ", new Exception());
                    return;
                }
            }
        }
    }

    public void setDone(boolean done) {
        synchronized (this) {
            this.done = done;
            if (done)
                this.notifyAll();
        }
    }
}
