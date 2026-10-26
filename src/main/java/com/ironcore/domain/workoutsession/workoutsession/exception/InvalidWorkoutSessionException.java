package com.ironcore.domain.workoutsession.workoutsession.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidWorkoutSessionException extends DomainException {
    public InvalidWorkoutSessionException(String message) {
        super(message);
    }
}
