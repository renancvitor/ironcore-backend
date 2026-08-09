package com.ironcore.domain.workoutplanning.workoutday.valueobject;

import com.ironcore.domain.workoutplanning.workoutday.exception.InvalidWorkoutDayException;

public record WorkoutDayId(Long value) {

    public WorkoutDayId {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutDayException("Id do dia de treino deve ser positivo.");
        }
    }
}
