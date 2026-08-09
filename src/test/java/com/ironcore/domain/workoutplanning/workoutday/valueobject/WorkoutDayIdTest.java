package com.ironcore.domain.workoutplanning.workoutday.valueobject;

import com.ironcore.domain.workoutplanning.workoutday.exception.InvalidWorkoutDayException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutDayIdTest {

    @Test
    void shouldCreateWorkoutDayIdWhenValueIsPositive() {
        WorkoutDayId workoutDayId = new WorkoutDayId(1L);

        assertThat(workoutDayId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenWorkoutDayIdIsNull() {
        assertThatThrownBy(() -> new WorkoutDayId(null))
                .isInstanceOf(InvalidWorkoutDayException.class);
    }

    @Test
    void shouldFailWhenWorkoutDayIdIsZero() {
        assertThatThrownBy(() -> new WorkoutDayId(0L))
                .isInstanceOf(InvalidWorkoutDayException.class);
    }

    @Test
    void shouldFailWhenWorkoutDayIdIsNegative() {
        assertThatThrownBy(() -> new WorkoutDayId(-1L))
                .isInstanceOf(InvalidWorkoutDayException.class);
    }
}
