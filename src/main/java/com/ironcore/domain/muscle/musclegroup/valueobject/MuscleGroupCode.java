package com.ironcore.domain.muscle.musclegroup.valueobject;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;

public record MuscleGroupCode(String value) {

    public MuscleGroupCode {
        if (value == null || value.isBlank()) {
            throw new InvalidMuscleGroupException("Código do grupo muscular é obrigatório.");
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9_]+")) {
            throw new InvalidMuscleGroupException("Código do grupo muscular deve conter apenas letras maiúsculas" +
                    " números e underscores.");
        }

        if (value.length() > 50) {
            throw new InvalidMuscleGroupException("Código do grupo muscular não pode exceder 50 caracteres.");
        }
    }
}
