package com.ironcore.application.exception;

public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }
}
