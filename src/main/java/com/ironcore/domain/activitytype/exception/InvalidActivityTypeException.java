package com.ironcore.domain.activitytype.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidActivityTypeException extends DomainException {

    public InvalidActivityTypeException(String message) {
        super(message);
    }
}
