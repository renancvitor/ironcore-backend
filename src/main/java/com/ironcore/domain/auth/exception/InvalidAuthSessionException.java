package com.ironcore.domain.auth.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidAuthSessionException extends DomainException {

    public InvalidAuthSessionException(String message) {
        super(message);
    }
}
