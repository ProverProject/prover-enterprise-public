package io.prover.common.transport.base;

public abstract class Session {

    public final String sessionKey;

    public Session(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public abstract boolean expired();

    public abstract long expireTime();
}
