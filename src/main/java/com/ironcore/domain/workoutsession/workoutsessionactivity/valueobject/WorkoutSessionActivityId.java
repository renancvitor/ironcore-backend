package com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject;

import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;

public record WorkoutSessionActivityId(Long value) {
    public WorkoutSessionActivityId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutSessionActivityException("Identificador deve ser positivo.");
        }
    }
}
