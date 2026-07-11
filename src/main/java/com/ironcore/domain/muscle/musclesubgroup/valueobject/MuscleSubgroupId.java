package com.ironcore.domain.muscle.musclesubgroup.valueobject;

import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;

public record MuscleSubgroupId(Long value) {

    public MuscleSubgroupId {
        if (value == null || value <= 0) {
            throw new InvalidMuscleSubgroupException("Id do subgrupo muscular deve ser positivo.");
        }
    }
}
