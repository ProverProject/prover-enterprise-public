package io.prover.common.transport.base;

import android.os.Handler;
import android.os.Looper;

public class SessionManager {
    public static final SessionManager INSTANCE = new SessionManager();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private RenewSessionCallback renewCallback;

    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        long expireIn = session.expireTime() - System.currentTimeMillis();
        handler.postDelayed(() -> {
            RenewSessionCallback callback = renewCallback;
            if (callback != null)
                callback.renewSession();
        }, expireIn - 5000);
    }

    public boolean isSessionValid() {
        return session != null && !session.expired();
    }

    public void setRenewCallback(RenewSessionCallback renewCallback) {
        this.renewCallback = renewCallback;
    }

    public interface RenewSessionCallback {
        void renewSession();
    }
}
