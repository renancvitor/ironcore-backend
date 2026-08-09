package com.ironcore.domain.workoutplanning.workoutday.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidWorkoutDayException extends DomainException {

    public InvalidWorkoutDayException(String message) {
        super(message);
    }
}
