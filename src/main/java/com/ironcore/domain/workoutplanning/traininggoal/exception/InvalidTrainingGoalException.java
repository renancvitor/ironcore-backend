package com.ironcore.domain.workoutplanning.traininggoal.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidTrainingGoalException extends DomainException {

    public InvalidTrainingGoalException(String message) {
        super(message);
    }
}
