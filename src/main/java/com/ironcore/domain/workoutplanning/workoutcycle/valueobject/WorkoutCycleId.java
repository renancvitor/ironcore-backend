package com.ironcore.domain.workoutplanning.workoutcycle.valueobject;

import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;

public record WorkoutCycleId(Long value) {

    public WorkoutCycleId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutCycleException("Id do ciclo de treino deve ser positivo.");
        }
    }
}
