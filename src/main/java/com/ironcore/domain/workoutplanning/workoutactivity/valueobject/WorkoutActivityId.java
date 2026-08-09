package com.ironcore.domain.workoutplanning.workoutactivity.valueobject;

import com.ironcore.domain.workoutplanning.workoutactivity.exception.InvalidWorkoutActivityException;

public record WorkoutActivityId(Long value) {

    public WorkoutActivityId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutActivityException("Id da atividade de treino deve ser positivo.");
        }
    }
}
