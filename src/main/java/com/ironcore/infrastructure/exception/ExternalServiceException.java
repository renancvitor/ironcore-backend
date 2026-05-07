package com.ironcore.infrastructure.exception;

public class ExternalServiceException extends InfrastructureException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
