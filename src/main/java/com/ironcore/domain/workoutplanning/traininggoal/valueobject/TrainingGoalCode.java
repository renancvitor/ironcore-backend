package com.ironcore.domain.workoutplanning.traininggoal.valueobject;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;

public record TrainingGoalCode(String value) {

    public TrainingGoalCode {
        if (value == null || value.isBlank()) {
            throw new InvalidTrainingGoalException("Código do objetivo de treino é obrigatório.");
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9_]+")) {
            throw new InvalidTrainingGoalException("Código do objetivo de treino deve conter apenas letras maiúsculas," +
                    " números e underscores.");
        }

        if (value.length() > 50) {
            throw new InvalidTrainingGoalException("Código do objetivo de treino não pode exceder 50 caracteres.");
        }
    }
}
