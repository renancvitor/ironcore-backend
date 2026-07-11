package com.ironcore.domain.muscle.musclegroup.valueobject;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuscleGroupIdTest {

    @Test
    void shouldFailWhenMuscleGroupIdIsNull() {
        assertThatThrownBy(() -> new MuscleGroupId(null))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }

    @Test
    void shouldFailWhenMuscleGroupIdIsZero() {
        assertThatThrownBy(() -> new MuscleGroupId(0L))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }

    @Test
    void shouldFailWhenMuscleGroupIdIsNegative() {
        assertThatThrownBy(() -> new MuscleGroupId(-1L))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }
}
