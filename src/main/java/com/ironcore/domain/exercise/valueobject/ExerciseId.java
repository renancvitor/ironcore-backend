package com.ironcore.domain.exercise.valueobject;

import com.ironcore.domain.exercise.exception.InvalidExerciseException;

public record ExerciseId(Long value) {

    public ExerciseId {

        if (value == null || value <= 0) {
            throw new InvalidExerciseException("Id do exercício deve ser positivo.");
        }
    }
}
