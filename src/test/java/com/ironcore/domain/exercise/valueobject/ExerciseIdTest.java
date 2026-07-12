package com.ironcore.domain.exercise.valueobject;

import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExerciseIdTest {

    @Test
    void shouldFailWhenExerciseIdIsNull() {
        assertThatThrownBy(() -> new ExerciseId(null))
                .isInstanceOf(InvalidExerciseException.class);
    }

    @Test
    void shouldFailWhenExerciseIdIsZero() {
        assertThatThrownBy(() -> new ExerciseId(0L))
                .isInstanceOf(InvalidExerciseException.class);
    }

    @Test
    void shouldFailWhenExerciseIdIsNegative() {
        assertThatThrownBy(() -> new ExerciseId(-1L))
                .isInstanceOf(InvalidExerciseException.class);
    }
}
