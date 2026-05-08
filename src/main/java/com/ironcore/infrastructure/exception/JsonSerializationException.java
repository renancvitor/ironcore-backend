package com.ironcore.infrastructure.exception;

public class JsonSerializationException extends InfrastructureException {

    public JsonSerializationException(String message) {
        super(message);
    }

    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
