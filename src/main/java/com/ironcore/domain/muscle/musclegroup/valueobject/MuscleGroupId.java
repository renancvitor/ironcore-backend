package com.ironcore.domain.muscle.musclegroup.valueobject;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;

public record MuscleGroupId(Long value) {

    public MuscleGroupId {
        if (value == null || value <= 0) {
            throw new InvalidMuscleGroupException("Id do grupo muscular deve ser positivo.");
        }
    }
}
