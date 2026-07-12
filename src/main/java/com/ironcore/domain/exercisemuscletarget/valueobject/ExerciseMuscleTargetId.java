package com.ironcore.domain.exercisemuscletarget.valueobject;

import com.ironcore.domain.exercisemuscletarget.exception.InvalidExerciseMuscleTargetException;

public record ExerciseMuscleTargetId(Long value) {

    public ExerciseMuscleTargetId {

        if (value == null || value <= 0) {
            throw new InvalidExerciseMuscleTargetException("Id do músculo alvo do exercício deve ser positivo.");
        }
    }
}
