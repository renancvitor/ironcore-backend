package com.ironcore.domain.workoutplanning.traininggoal.valueobject;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;

public record TrainingGoalId(Long value) {

    public TrainingGoalId {
        if (value == null || value <= 0) {
            throw new InvalidTrainingGoalException("Id do objetivo de treino deve ser positivo.");
        }
    }
}
