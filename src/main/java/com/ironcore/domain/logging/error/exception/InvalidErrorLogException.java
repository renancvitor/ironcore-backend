package com.ironcore.domain.logging.error.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidErrorLogException extends DomainException {

    public InvalidErrorLogException(String message) {
        super(message);
    }
}
