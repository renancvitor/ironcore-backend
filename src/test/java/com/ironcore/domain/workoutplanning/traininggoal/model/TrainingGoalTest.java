package com.ironcore.domain.workoutplanning.traininggoal.model;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.restoreTrainingGoal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TrainingGoalTest {

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingTrainingGoal() {
            TrainingGoal trainingGoal = restoreTrainingGoal();

            assertThat(trainingGoal.getId()).isEqualTo(new TrainingGoalId(1L));
            assertThat(trainingGoal.getCode()).isEqualTo(new TrainingGoalCode("HYPERTROPHY"));
            assertThat(trainingGoal.getDisplayName()).isEqualTo("Hipertrofia");
            assertThat(trainingGoal.getActive()).isTrue();
            assertThat(trainingGoal.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankDisplayName() {
            assertThatExceptionOfType(InvalidTrainingGoalException.class)
                    .isThrownBy(() -> TrainingGoal.restore(
                            new TrainingGoalId(1L),
                            new TrainingGoalCode("HYPERTROPHY"),
                            " ",
                            true,
                            10
                    ))
                    .withMessage("Nome de exibição não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidTrainingGoalException.class)
                    .isThrownBy(() -> TrainingGoal.restore(
                            null,
                            new TrainingGoalCode("HYPERTROPHY"),
                            "Hipertrofia",
                            true,
                            10
                    ))
                    .withMessage("Id não pode ser nulo.");
        }
    }
}
