package com.ironcore.domain.user.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidEmailException extends DomainException {

    public InvalidEmailException(String message) {
        super(message);
    }
}
