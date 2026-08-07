package com.ironcore.domain.workoutplanning.traininggoal.valueobject;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingGoalCodeTest {

    @Test
    void shouldNormalizeCode() {
        TrainingGoalCode code = code(" hypertrophy ");

        assertThat(code.value()).isEqualTo("HYPERTROPHY");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new TrainingGoalCode(null))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }

    @Test
    void shouldRejectWhenCodeIsBlank() {
        assertThatThrownBy(() -> new TrainingGoalCode(" "))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }

    @Test
    void shouldRejectInvalidCodeFormat() {
        assertThatThrownBy(() -> new TrainingGoalCode("invalid-@"))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }

    @Test
    void shouldRejectLengthGreaterThan50() {
        assertThatThrownBy(() -> new TrainingGoalCode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .isInstanceOf(InvalidTrainingGoalException.class);
    }
}
