package com.ironcore.domain.user.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidPasswordException extends DomainException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
