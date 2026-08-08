package com.ironcore.domain.workoutplanning.workoutcycle.valueobject;

import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutCycleIdTest {

    @Test
    void shouldCreateWorkoutCycleIdWhenValueIsPositive() {
        WorkoutCycleId workoutCycleId = new WorkoutCycleId(1L);

        assertThat(workoutCycleId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenWorkoutCycleIdIsNull() {
        assertThatThrownBy(() -> new WorkoutCycleId(null))
                .isInstanceOf(InvalidWorkoutCycleException.class);
    }

    @Test
    void shouldFailWhenWorkoutCycleIdIsZero() {
        assertThatThrownBy(() -> new WorkoutCycleId(0L))
                .isInstanceOf(InvalidWorkoutCycleException.class);
    }

    @Test
    void shouldFailWhenWorkoutCycleIdIsNegative() {
        assertThatThrownBy(() -> new WorkoutCycleId(-1L))
                .isInstanceOf(InvalidWorkoutCycleException.class);
    }
}
