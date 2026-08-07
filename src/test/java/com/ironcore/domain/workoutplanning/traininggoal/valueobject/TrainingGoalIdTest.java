package com.ironcore.domain.workoutplanning.traininggoal.valueobject;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingGoalIdTest {

    @Test
    void shouldFailWhenTrainingGoalIdIsNull() {
        assertThatThrownBy(() -> new TrainingGoalId(null))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }

    @Test
    void shouldFailWhenTrainingGoalIdIsZero() {
        assertThatThrownBy(() -> new TrainingGoalId(0L))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }

    @Test
    void shouldFailWhenTrainingGoalIdIsNegative() {
        assertThatThrownBy(() -> new TrainingGoalId(-1L))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }
}
