package com.ironcore.domain.muscle.musclesubgroup.valueobject;

import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuscleSubgroupIdTest {

    @Test
    void shouldFailWhenMuscleSubgroupIdIsNull() {
        assertThatThrownBy(() -> new MuscleSubgroupId(null))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }

    @Test
    void shouldFailWhenMuscleSubgroupIdIsZero() {
        assertThatThrownBy(() -> new MuscleSubgroupId(0L))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }

    @Test
    void shouldFailWhenMuscleSubgroupIdIsNegative() {
        assertThatThrownBy(() -> new MuscleSubgroupId(-1L))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }
}
