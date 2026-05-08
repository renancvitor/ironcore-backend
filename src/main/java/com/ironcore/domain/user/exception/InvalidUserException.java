package com.ironcore.domain.user.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidUserException extends DomainException {

    public InvalidUserException(String message) {
        super(message);
    }
}
