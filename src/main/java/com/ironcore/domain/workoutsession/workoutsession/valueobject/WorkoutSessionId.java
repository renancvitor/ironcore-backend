package com.ironcore.domain.workoutsession.workoutsession.valueobject;

import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;

public record WorkoutSessionId(Long value) {
    public WorkoutSessionId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutSessionException("Identificador deve ser positivo.");
        }
    }
}
