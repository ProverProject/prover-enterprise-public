package io.prover.common.transport.exception;

public class RegisterException extends ServerException {

    public RegisterException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
