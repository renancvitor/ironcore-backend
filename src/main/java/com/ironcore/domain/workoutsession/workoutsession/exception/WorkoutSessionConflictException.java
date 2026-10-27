package com.ironcore.domain.workoutsession.workoutsession.exception;

import com.ironcore.domain.exception.DomainException;

public class WorkoutSessionConflictException extends DomainException {

    public WorkoutSessionConflictException(String message) {
        super(message);
    }

    public WorkoutSessionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
