package com.ironcore.domain.workoutplanning.workoutcycle.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidWorkoutCycleException extends DomainException {

    public InvalidWorkoutCycleException(String message) {
        super(message);
    }
}
