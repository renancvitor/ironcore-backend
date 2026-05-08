package com.ironcore.infrastructure.exception;

public class PersistenceException extends InfrastructureException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
