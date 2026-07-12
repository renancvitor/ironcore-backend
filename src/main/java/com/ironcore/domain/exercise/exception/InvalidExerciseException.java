package com.ironcore.domain.exercise.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidExerciseException extends DomainException {

    public InvalidExerciseException(String message) {
        super(message);
    }
}
