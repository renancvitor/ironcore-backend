package com.ironcore.domain.workoutplanning.workoutcycle.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WorkoutCycleTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterWorkoutCycleWithInitialState() {
            WorkoutCycle workoutCycle = WorkoutCycle.register(
                    PERSON_ID,
                    " Ciclo de hipertrofia ",
                    TRAINING_GOAL_ID,
                    3,
                    WorkoutOrigin.MANUAL,
                    "Planejamento inicial.",
                    CREATED_AT
            );

            assertThat(workoutCycle.getId()).isNull();
            assertThat(workoutCycle.getPersonId()).isEqualTo(PERSON_ID);
            assertThat(workoutCycle.getName()).isEqualTo("Ciclo de hipertrofia");
            assertThat(workoutCycle.getTrainingGoalId()).isEqualTo(TRAINING_GOAL_ID);
            assertThat(workoutCycle.getStartDate()).isNull();
            assertThat(workoutCycle.getEndDate()).isNull();
            assertThat(workoutCycle.getDesiredDurationMonths()).isEqualTo(3);
            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.NOT_STARTED);
            assertThat(workoutCycle.getWorkoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(workoutCycle.getNotes()).isEqualTo("Planejamento inicial.");
            assertThat(workoutCycle.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutCycle.getUpdatedAt()).isNull();
        }

        @Test
        void shouldRestoreExistingWorkoutCycleState() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThat(workoutCycle.getId()).isEqualTo(WORKOUT_CYCLE_ID);
            assertThat(workoutCycle.getPersonId()).isEqualTo(PERSON_ID);
            assertThat(workoutCycle.getName()).isEqualTo("Ciclo de hipertrofia");
            assertThat(workoutCycle.getTrainingGoalId()).isEqualTo(TRAINING_GOAL_ID);
            assertThat(workoutCycle.getStartDate()).isEqualTo(START_DATE);
            assertThat(workoutCycle.getEndDate()).isNull();
            assertThat(workoutCycle.getDesiredDurationMonths()).isEqualTo(3);
            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getWorkoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(workoutCycle.getNotes()).isEqualTo("Planejamento restaurado.");
            assertThat(workoutCycle.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class BusinessChanges {

        @Test
        void shouldUpdateEditablePlanningData() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();
            TrainingGoalId newTrainingGoalId = new TrainingGoalId(2L);

            workoutCycle.updateCycle(
                    " Novo planejamento ",
                    newTrainingGoalId,
                    6,
                    "Novas observações.",
                    UPDATED_AT
            );

            assertThat(workoutCycle.getName()).isEqualTo("Novo planejamento");
            assertThat(workoutCycle.getTrainingGoalId()).isEqualTo(newTrainingGoalId);
            assertThat(workoutCycle.getDesiredDurationMonths()).isEqualTo(6);
            assertThat(workoutCycle.getNotes()).isEqualTo("Novas observações.");
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldClearOptionalPlanningData() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            workoutCycle.updateCycle(
                    "Ciclo de hipertrofia",
                    TRAINING_GOAL_ID,
                    null,
                    null,
                    UPDATED_AT
            );

            assertThat(workoutCycle.getDesiredDurationMonths()).isNull();
            assertThat(workoutCycle.getNotes()).isNull();
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class Atomicity {

        @Test
        void shouldPreserveStateWhenDurationUpdateIsInvalid() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.updateCycle(
                            "Novo planejamento",
                            new TrainingGoalId(2L),
                            0,
                            "Novas observações.",
                            UPDATED_AT
                    ))
                    .withMessage("Duração desejada deve ser positiva.");

            assertInitialEditableState(workoutCycle);
        }

        @Test
        void shouldPreserveStateWhenUpdateDateIsMissing() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.updateCycle(
                            "Novo planejamento",
                            new TrainingGoalId(2L),
                            6,
                            "Novas observações.",
                            null
                    ))
                    .withMessage("Data de atualização é obrigatória.");

            assertInitialEditableState(workoutCycle);
        }

        private void assertInitialEditableState(WorkoutCycle workoutCycle) {
            assertThat(workoutCycle.getName()).isEqualTo("Ciclo de hipertrofia");
            assertThat(workoutCycle.getTrainingGoalId()).isEqualTo(TRAINING_GOAL_ID);
            assertThat(workoutCycle.getDesiredDurationMonths()).isEqualTo(3);
            assertThat(workoutCycle.getNotes()).isEqualTo("Planejamento inicial.");
            assertThat(workoutCycle.getUpdatedAt()).isNull();
        }
    }

    @Nested
    class Lifecycle {

        @Test
        void shouldStartNotStartedWorkoutCycle() {
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);

            workoutCycle.startCycle(START_DATE);

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getStartDate()).isEqualTo(START_DATE);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldRejectStartingWorkoutCycleThatWasAlreadyStarted() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.startCycle(START_DATE.plusDays(1)))
                    .withMessage("Somente um ciclo não iniciado pode ser iniciado.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getStartDate()).isEqualTo(START_DATE);
        }

        @Test
        void shouldRequireStartDate() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.startCycle(null))
                    .withMessage("Data de início é obrigatória.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.NOT_STARTED);
            assertThat(workoutCycle.getStartDate()).isNull();
        }

        @Test
        void shouldCompleteWorkoutCycleInProgress() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            workoutCycle.endCycle(END_DATE);

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.COMPLETED);
            assertThat(workoutCycle.getEndDate()).isEqualTo(END_DATE);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldRejectCompletingWorkoutCycleThatIsNotInProgress() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.endCycle(END_DATE))
                    .withMessage("Somente um ciclo em andamento pode ser concluído.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.NOT_STARTED);
            assertThat(workoutCycle.getEndDate()).isNull();
        }

        @Test
        void shouldRequireEndDate() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.endCycle(null))
                    .withMessage("Data de conclusão é obrigatória.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getEndDate()).isNull();
        }

        @Test
        void shouldRejectEndDateBeforeStartDate() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> workoutCycle.endCycle(START_DATE.minusDays(1)))
                    .withMessage("Data de conclusão não pode ser anterior à data de início.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getEndDate()).isNull();
        }

        @Test
        void shouldCancelNotStartedWorkoutCycle() {
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);

            workoutCycle.cancelCycle();

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.CANCELLED);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldCancelWorkoutCycleInProgress() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            workoutCycle.cancelCycle();

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.CANCELLED);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldRejectCancellingCompletedWorkoutCycle() {
            WorkoutCycle workoutCycle = completedWorkoutCycle();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(workoutCycle::cancelCycle)
                    .withMessage("Um ciclo concluído ou cancelado não pode ser cancelado.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.COMPLETED);
        }

        @Test
        void shouldRejectCancellingAlreadyCancelledWorkoutCycle() {
            WorkoutCycle workoutCycle = cancelledWorkoutCycle();

            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(workoutCycle::cancelCycle)
                    .withMessage("Um ciclo concluído ou cancelado não pode ser cancelado.");

            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.CANCELLED);
        }
    }

    @Nested
    class ProgressCalculation {

        @Test
        void shouldNotCalculateProgressWithoutDesiredDuration() {
            WorkoutCycle workoutCycle = WorkoutCycle.register(
                    PERSON_ID,
                    "Ciclo sem duração",
                    TRAINING_GOAL_ID,
                    null,
                    WorkoutOrigin.MANUAL,
                    null,
                    CREATED_AT
            );
            workoutCycle.startCycle(START_DATE);

            assertThat(workoutCycle.calculateProgress(START_DATE.plusDays(1))).isNull();
        }

        @Test
        void shouldNotCalculateProgressBeforeWorkoutCycleStarts() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();

            assertThat(workoutCycle.calculateProgress(START_DATE)).isNull();
        }

        @Test
        void shouldReturnZeroProgressAtOrBeforeStartDate() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();
            workoutCycle.startCycle(START_DATE);

            assertThat(workoutCycle.calculateProgress(START_DATE)).isZero();
            assertThat(workoutCycle.calculateProgress(START_DATE.minusDays(1))).isZero();
        }

        @Test
        void shouldCalculateProgressDuringWorkoutCycle() {
            WorkoutCycle workoutCycle = WorkoutCycle.register(
                    PERSON_ID,
                    "Ciclo mensal",
                    TRAINING_GOAL_ID,
                    1,
                    WorkoutOrigin.MANUAL,
                    null,
                    CREATED_AT
            );
            workoutCycle.startCycle(START_DATE);

            assertThat(workoutCycle.calculateProgress(START_DATE.plusDays(16))).isEqualTo(51);
        }

        @Test
        void shouldLimitProgressToOneHundredPercent() {
            WorkoutCycle workoutCycle = workoutCycleWithoutId();
            workoutCycle.startCycle(START_DATE);
            LocalDate expectedEndDate = START_DATE.plusMonths(3);

            assertThat(workoutCycle.calculateProgress(expectedEndDate)).isEqualTo(100);
            assertThat(workoutCycle.calculateProgress(expectedEndDate.plusDays(1))).isEqualTo(100);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequirePerson() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            null,
                            "Ciclo de hipertrofia",
                            TRAINING_GOAL_ID,
                            3,
                            WorkoutOrigin.MANUAL,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Pessoa não pode ser nula.");
        }

        @Test
        void shouldRejectBlankName() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            " ",
                            TRAINING_GOAL_ID,
                            3,
                            WorkoutOrigin.MANUAL,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Nome não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireTrainingGoal() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            "Ciclo de hipertrofia",
                            null,
                            3,
                            WorkoutOrigin.MANUAL,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Objetivo de treino é obrigatório.");
        }

        @Test
        void shouldRejectZeroDesiredDuration() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            "Ciclo de hipertrofia",
                            TRAINING_GOAL_ID,
                            0,
                            WorkoutOrigin.MANUAL,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Duração desejada deve ser positiva.");
        }

        @Test
        void shouldRejectNegativeDesiredDuration() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            "Ciclo de hipertrofia",
                            TRAINING_GOAL_ID,
                            -1,
                            WorkoutOrigin.MANUAL,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Duração desejada deve ser positiva.");
        }

        @Test
        void shouldRequireWorkoutOrigin() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            "Ciclo de hipertrofia",
                            TRAINING_GOAL_ID,
                            3,
                            null,
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Origem do treino é obrigatório.");
        }

        @Test
        void shouldRequireCreationDate() {
            assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                    .isThrownBy(() -> WorkoutCycle.register(
                            PERSON_ID,
                            "Ciclo de hipertrofia",
                            TRAINING_GOAL_ID,
                            3,
                            WorkoutOrigin.MANUAL,
                            null,
                            null
                    ))
                    .withMessage("Data de criação é obrigatória.");
        }
    }
}
