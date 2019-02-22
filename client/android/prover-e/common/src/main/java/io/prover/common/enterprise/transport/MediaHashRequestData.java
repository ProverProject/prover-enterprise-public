package io.prover.common.enterprise.transport;

import io.prover.common.enterprise.transport.response.SwypeCodeFastReply;
import io.prover.common.enterprise.transport.response.SwypeCodeReply;

public class MediaHashRequestData {

    public final byte[] digest;

    public final MediHashType hashType;
    public final int cliendId;

    public final SwypeCodeFastReply swypeCodeFastReply;
    public final SwypeCodeReply swypeCodeReply;

    public MediaHashRequestData(byte[] digest, MediHashType hashType, int cliendId, SwypeCodeFastReply swypeCodeFastReply) {
        this.digest = digest;
        this.hashType = hashType;
        this.cliendId = cliendId;
        this.swypeCodeFastReply = swypeCodeFastReply;
        this.swypeCodeReply = null;
    }

    public MediaHashRequestData(byte[] digest, MediHashType hashType, int cliendId, SwypeCodeReply swypeCodeReply) {
        this.digest = digest;
        this.hashType = hashType;
        this.cliendId = cliendId;
        this.swypeCodeFastReply = null;
        this.swypeCodeReply = swypeCodeReply;
    }

    public enum MediHashType {
        SHA256;

        public String toServerString() {
            switch (this) {
                case SHA256:
                    return "SHA-256";

                default:
                    throw new RuntimeException("Not implemented for: " + this.name());
            }
        }
    }
}
