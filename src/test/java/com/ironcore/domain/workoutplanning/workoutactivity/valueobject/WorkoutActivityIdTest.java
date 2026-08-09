package com.ironcore.domain.workoutplanning.workoutactivity.valueobject;

import com.ironcore.domain.workoutplanning.workoutactivity.exception.InvalidWorkoutActivityException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutActivityIdTest {

    @Test
    void shouldCreateWorkoutActivityIdWhenValueIsPositive() {
        WorkoutActivityId workoutActivityId = new WorkoutActivityId(1L);

        assertThat(workoutActivityId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenWorkoutActivityIdIsNull() {
        assertThatThrownBy(() -> new WorkoutActivityId(null))
                .isInstanceOf(InvalidWorkoutActivityException.class);
    }

    @Test
    void shouldFailWhenWorkoutActivityIdIsZero() {
        assertThatThrownBy(() -> new WorkoutActivityId(0L))
                .isInstanceOf(InvalidWorkoutActivityException.class);
    }

    @Test
    void shouldFailWhenWorkoutActivityIdIsNegative() {
        assertThatThrownBy(() -> new WorkoutActivityId(-1L))
                .isInstanceOf(InvalidWorkoutActivityException.class);
    }
}
