package com.ironcore.domain.exercisemuscletarget.valueobject;

import com.ironcore.domain.exercisemuscletarget.exception.InvalidExerciseMuscleTargetException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExerciseMuscleTargetIdTest {

    @Test
    void shouldFailWhenExerciseMuscleTargetIdIsNull() {
        assertThatThrownBy(() -> new ExerciseMuscleTargetId(null))
                .isInstanceOf(InvalidExerciseMuscleTargetException.class);
    }

    @Test
    void shouldFailWhenExerciseMuscleTargetIdIsZero() {
        assertThatThrownBy(() -> new ExerciseMuscleTargetId(0L))
                .isInstanceOf(InvalidExerciseMuscleTargetException.class);
    }

    @Test
    void shouldFailWhenExerciseMuscleTargetIdIsNegative() {
        assertThatThrownBy(() -> new ExerciseMuscleTargetId(-1L))
                .isInstanceOf(InvalidExerciseMuscleTargetException.class);
    }
}
