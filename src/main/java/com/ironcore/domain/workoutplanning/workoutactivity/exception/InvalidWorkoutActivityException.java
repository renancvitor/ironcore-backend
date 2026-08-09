package com.ironcore.domain.workoutplanning.workoutactivity.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidWorkoutActivityException extends DomainException {

    public InvalidWorkoutActivityException(String message) {
        super(message);
    }
}
