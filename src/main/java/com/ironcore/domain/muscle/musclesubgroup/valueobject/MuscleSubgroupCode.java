package com.ironcore.domain.muscle.musclesubgroup.valueobject;

import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;

public record MuscleSubgroupCode(String value) {

    public MuscleSubgroupCode {
        if (value == null || value.isBlank()) {
            throw new InvalidMuscleSubgroupException("Código do subgrupo muscular é obrigatório.");
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9_]+")) {
            throw new InvalidMuscleSubgroupException("Código do subgrupo muscular deve conter apenas letras" +
                    " maiúsculas números e underscores.");
        }

        if (value.length() > 50) {
            throw new InvalidMuscleSubgroupException("Código do subgrupo muscular não pode exceder 50 caracteres.");
        }
    }
}
