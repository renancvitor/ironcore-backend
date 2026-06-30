package com.ironcore.domain.person.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidPersonException extends DomainException {
    public InvalidPersonException(String message) {
        super(message);
    }
}
