package com.ironcore.infrastructure.exception;

public class DataMappingException extends InfrastructureException {

    public DataMappingException(String message) {
        super(message);
    }

    public DataMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
