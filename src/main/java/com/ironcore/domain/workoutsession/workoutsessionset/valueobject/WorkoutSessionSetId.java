package com.ironcore.domain.workoutsession.workoutsessionset.valueobject;

import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;

public record WorkoutSessionSetId(Long value) {
    public WorkoutSessionSetId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutSessionSetException("Identificador deve ser positivo.");
        }
    }
}
