package io.prover.common.transport.exception;

import java.io.IOException;

public class ServerException extends IOException {
    public final int errorCode;
    public final String errorMessage;

    public ServerException(int errorCode, String errorMessage) {
        super(limitText(errorMessage));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private static String limitText(String text) {
        return text.length() <= 64 ? text : text.substring(0, 64) + "...";
    }
}
