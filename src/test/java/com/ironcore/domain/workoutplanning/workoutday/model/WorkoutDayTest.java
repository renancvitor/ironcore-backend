package com.ironcore.domain.workoutplanning.workoutday.model;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.exception.InvalidWorkoutDayException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WorkoutDayTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterWorkoutDayWithoutId() {
            WorkoutDay workoutDay = WorkoutDay.register(
                    WORKOUT_CYCLE_ID,
                    WeekDay.MONDAY,
                    " Treino de membros superiores ",
                    1,
                    CREATED_AT
            );

            assertThat(workoutDay.getId()).isNull();
            assertThat(workoutDay.getWorkoutCycleId()).isEqualTo(WORKOUT_CYCLE_ID);
            assertThat(workoutDay.getWeekDay()).isEqualTo(WeekDay.MONDAY);
            assertThat(workoutDay.getTitle()).isEqualTo("Treino de membros superiores");
            assertThat(workoutDay.getOrderIndex()).isEqualTo(1);
            assertThat(workoutDay.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutDay.getUpdatedAt()).isNull();
        }

        @Test
        void shouldRestoreExistingWorkoutDayState() {
            WorkoutDay workoutDay = restoredWorkoutDay();

            assertThat(workoutDay.getId()).isEqualTo(WORKOUT_DAY_ID);
            assertThat(workoutDay.getWorkoutCycleId()).isEqualTo(WORKOUT_CYCLE_ID);
            assertThat(workoutDay.getWeekDay()).isEqualTo(WeekDay.WEDNESDAY);
            assertThat(workoutDay.getTitle()).isEqualTo("Treino de membros inferiores");
            assertThat(workoutDay.getOrderIndex()).isEqualTo(2);
            assertThat(workoutDay.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutDay.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class BusinessChanges {

        @Test
        void shouldUpdateEditableWorkoutDayData() {
            WorkoutDay workoutDay = workoutDayWithoutId();

            workoutDay.updateDay(
                    WeekDay.FRIDAY,
                    " Treino de corpo inteiro ",
                    3,
                    UPDATED_AT
            );

            assertThat(workoutDay.getWeekDay()).isEqualTo(WeekDay.FRIDAY);
            assertThat(workoutDay.getTitle()).isEqualTo("Treino de corpo inteiro");
            assertThat(workoutDay.getOrderIndex()).isEqualTo(3);
            assertThat(workoutDay.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class Atomicity {

        @Test
        void shouldPreserveStateWhenSortOrderUpdateIsInvalid() {
            WorkoutDay workoutDay = workoutDayWithoutId();

            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> workoutDay.updateDay(
                            WeekDay.FRIDAY,
                            "Treino de corpo inteiro",
                            0,
                            UPDATED_AT
                    ))
                    .withMessage("Ordem de exibição deve ser maior que zero.");

            assertInitialEditableState(workoutDay);
        }

        @Test
        void shouldPreserveStateWhenUpdateDateIsMissing() {
            WorkoutDay workoutDay = workoutDayWithoutId();

            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> workoutDay.updateDay(
                            WeekDay.FRIDAY,
                            "Treino de corpo inteiro",
                            3,
                            null
                    ))
                    .withMessage("Data de atualização é obrigatória.");

            assertInitialEditableState(workoutDay);
        }

        private void assertInitialEditableState(WorkoutDay workoutDay) {
            assertThat(workoutDay.getWeekDay()).isEqualTo(WeekDay.MONDAY);
            assertThat(workoutDay.getTitle()).isEqualTo("Treino de membros superiores");
            assertThat(workoutDay.getOrderIndex()).isEqualTo(1);
            assertThat(workoutDay.getUpdatedAt()).isNull();
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequireWorkoutCycle() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            null,
                            WeekDay.MONDAY,
                            "Treino de membros superiores",
                            1,
                            CREATED_AT
                    ))
                    .withMessage("Ciclo de treino não pode ser nulo.");
        }

        @Test
        void shouldRequireWeekDay() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            null,
                            "Treino de membros superiores",
                            1,
                            CREATED_AT
                    ))
                    .withMessage("Dia da semana não pode ser nulo.");
        }

        @Test
        void shouldRejectBlankTitle() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            WeekDay.MONDAY,
                            " ",
                            1,
                            CREATED_AT
                    ))
                    .withMessage("Título não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireSortOrder() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            WeekDay.MONDAY,
                            "Treino de membros superiores",
                            null,
                            CREATED_AT
                    ))
                    .withMessage("Ordem de exibição deve ser maior que zero.");
        }

        @Test
        void shouldRejectZeroSortOrder() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            WeekDay.MONDAY,
                            "Treino de membros superiores",
                            0,
                            CREATED_AT
                    ))
                    .withMessage("Ordem de exibição deve ser maior que zero.");
        }

        @Test
        void shouldRejectNegativeSortOrder() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            WeekDay.MONDAY,
                            "Treino de membros superiores",
                            -1,
                            CREATED_AT
                    ))
                    .withMessage("Ordem de exibição deve ser maior que zero.");
        }

        @Test
        void shouldRequireCreationDate() {
            assertThatExceptionOfType(InvalidWorkoutDayException.class)
                    .isThrownBy(() -> WorkoutDay.register(
                            WORKOUT_CYCLE_ID,
                            WeekDay.MONDAY,
                            "Treino de membros superiores",
                            1,
                            null
                    ))
                    .withMessage("Data de criação é obrigatória.");
        }
    }
}
