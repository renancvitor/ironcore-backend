package com.ironcore.domain.workoutplanning.workoutactivity.model;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.exception.InvalidWorkoutActivityException;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WorkoutActivityTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterWorkoutActivityWithoutId() {
            WorkoutActivity workoutActivity = WorkoutActivity.register(
                    WORKOUT_DAY_ID,
                    EXERCISE_ID,
                    1,
                    4,
                    8,
                    12,
                    new BigDecimal("80.50"),
                    " RPE 8 ",
                    45,
                    new BigDecimal("5.50"),
                    " Moderada ",
                    90,
                    " Priorizar a técnica ",
                    CREATED_AT
            );

            assertThat(workoutActivity.getId()).isNull();
            assertThat(workoutActivity.getWorkoutDayId()).isEqualTo(WORKOUT_DAY_ID);
            assertThat(workoutActivity.getExerciseId()).isEqualTo(EXERCISE_ID);
            assertThat(workoutActivity.getOrderIndex()).isEqualTo(1);
            assertThat(workoutActivity.getSets()).isEqualTo(4);
            assertThat(workoutActivity.getRepRangeMin()).isEqualTo(8);
            assertThat(workoutActivity.getRepRangeMax()).isEqualTo(12);
            assertThat(workoutActivity.getTargetLoadKg()).isEqualByComparingTo("80.50");
            assertThat(workoutActivity.getTargetLoadText()).isEqualTo("RPE 8");
            assertThat(workoutActivity.getDurationMinutes()).isEqualTo(45);
            assertThat(workoutActivity.getDistanceKm()).isEqualByComparingTo("5.50");
            assertThat(workoutActivity.getIntensityText()).isEqualTo("Moderada");
            assertThat(workoutActivity.getRestSeconds()).isEqualTo(90);
            assertThat(workoutActivity.getNotes()).isEqualTo("Priorizar a técnica");
            assertThat(workoutActivity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutActivity.getUpdatedAt()).isNull();
        }

        @Test
        void shouldRestoreExistingWorkoutActivityState() {
            WorkoutActivity workoutActivity = restoredWorkoutActivity();

            assertThat(workoutActivity.getId()).isEqualTo(WORKOUT_ACTIVITY_ID);
            assertThat(workoutActivity.getWorkoutDayId()).isEqualTo(WORKOUT_DAY_ID);
            assertThat(workoutActivity.getExerciseId()).isEqualTo(EXERCISE_ID);
            assertThat(workoutActivity.getOrderIndex()).isEqualTo(2);
            assertThat(workoutActivity.getSets()).isEqualTo(5);
            assertThat(workoutActivity.getRepRangeMin()).isEqualTo(6);
            assertThat(workoutActivity.getRepRangeMax()).isEqualTo(10);
            assertThat(workoutActivity.getTargetLoadKg()).isEqualByComparingTo("90.00");
            assertThat(workoutActivity.getTargetLoadText()).isEqualTo("RPE 9");
            assertThat(workoutActivity.getDurationMinutes()).isEqualTo(50);
            assertThat(workoutActivity.getDistanceKm()).isEqualByComparingTo("6.00");
            assertThat(workoutActivity.getIntensityText()).isEqualTo("Alta");
            assertThat(workoutActivity.getRestSeconds()).isEqualTo(120);
            assertThat(workoutActivity.getNotes()).isEqualTo("Manter cadência controlada");
            assertThat(workoutActivity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutActivity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class BusinessChanges {

        @Test
        void shouldUpdateEditableWorkoutActivityData() {
            WorkoutActivity workoutActivity = workoutActivityWithoutId();

            workoutActivity.updateActivity(
                    2,
                    5,
                    6,
                    10,
                    new BigDecimal("90.00"),
                    " RPE 9 ",
                    50,
                    new BigDecimal("6.00"),
                    " Alta ",
                    120,
                    " Manter cadência controlada ",
                    UPDATED_AT
            );

            assertThat(workoutActivity.getOrderIndex()).isEqualTo(2);
            assertThat(workoutActivity.getSets()).isEqualTo(5);
            assertThat(workoutActivity.getRepRangeMin()).isEqualTo(6);
            assertThat(workoutActivity.getRepRangeMax()).isEqualTo(10);
            assertThat(workoutActivity.getTargetLoadKg()).isEqualByComparingTo("90.00");
            assertThat(workoutActivity.getTargetLoadText()).isEqualTo("RPE 9");
            assertThat(workoutActivity.getDurationMinutes()).isEqualTo(50);
            assertThat(workoutActivity.getDistanceKm()).isEqualByComparingTo("6.00");
            assertThat(workoutActivity.getIntensityText()).isEqualTo("Alta");
            assertThat(workoutActivity.getRestSeconds()).isEqualTo(120);
            assertThat(workoutActivity.getNotes()).isEqualTo("Manter cadência controlada");
            assertThat(workoutActivity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldClearOptionalWorkoutActivityData() {
            WorkoutActivity workoutActivity = workoutActivityWithoutId();

            workoutActivity.updateActivity(
                    2,
                    5,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    UPDATED_AT
            );

            assertThat(workoutActivity.getRepRangeMin()).isNull();
            assertThat(workoutActivity.getRepRangeMax()).isNull();
            assertThat(workoutActivity.getTargetLoadKg()).isNull();
            assertThat(workoutActivity.getTargetLoadText()).isNull();
            assertThat(workoutActivity.getDurationMinutes()).isNull();
            assertThat(workoutActivity.getDistanceKm()).isNull();
            assertThat(workoutActivity.getIntensityText()).isNull();
            assertThat(workoutActivity.getRestSeconds()).isNull();
            assertThat(workoutActivity.getNotes()).isNull();
            assertThat(workoutActivity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class Atomicity {

        @Test
        void shouldPreserveStateWhenLateWorkoutActivityDataIsInvalid() {
            WorkoutActivity workoutActivity = workoutActivityWithoutId();

            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> workoutActivity.updateActivity(
                            2,
                            5,
                            6,
                            10,
                            new BigDecimal("90.00"),
                            "RPE 9",
                            50,
                            new BigDecimal("6.00"),
                            "Alta",
                            120,
                            " ",
                            UPDATED_AT
                    ))
                    .withMessage("Anotações não podem ser vazias.");

            assertInitialEditableState(workoutActivity);
        }

        @Test
        void shouldPreserveStateWhenUpdateDateIsMissing() {
            WorkoutActivity workoutActivity = workoutActivityWithoutId();

            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> workoutActivity.updateActivity(
                            2,
                            5,
                            6,
                            10,
                            new BigDecimal("90.00"),
                            "RPE 9",
                            50,
                            new BigDecimal("6.00"),
                            "Alta",
                            120,
                            "Manter cadência controlada",
                            null
                    ))
                    .withMessage("Data de atualização é obrigatória.");

            assertInitialEditableState(workoutActivity);
        }

        private void assertInitialEditableState(WorkoutActivity workoutActivity) {
            assertThat(workoutActivity.getOrderIndex()).isEqualTo(1);
            assertThat(workoutActivity.getSets()).isEqualTo(4);
            assertThat(workoutActivity.getRepRangeMin()).isEqualTo(8);
            assertThat(workoutActivity.getRepRangeMax()).isEqualTo(12);
            assertThat(workoutActivity.getTargetLoadKg()).isEqualByComparingTo("80.50");
            assertThat(workoutActivity.getTargetLoadText()).isEqualTo("RPE 8");
            assertThat(workoutActivity.getDurationMinutes()).isEqualTo(45);
            assertThat(workoutActivity.getDistanceKm()).isEqualByComparingTo("5.50");
            assertThat(workoutActivity.getIntensityText()).isEqualTo("Moderada");
            assertThat(workoutActivity.getRestSeconds()).isEqualTo(90);
            assertThat(workoutActivity.getNotes()).isEqualTo("Priorizar a técnica");
            assertThat(workoutActivity.getUpdatedAt()).isNull();
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequireWorkoutDay() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(null, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Dia de treino não pode ser nulo.");
        }

        @Test
        void shouldRequireExercise() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, null, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Exercício não pode ser nulo.");
        }

        @Test
        void shouldRequireOrderIndex() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, null, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Ordem deve ser maior que zero.");
        }

        @Test
        void shouldRejectZeroOrderIndex() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 0, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Ordem deve ser maior que zero.");
        }

        @Test
        void shouldRejectNegativeOrderIndex() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, -1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Ordem deve ser maior que zero.");
        }

        @Test
        void shouldRequireSets() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, null, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Quantidade de séries deve ser maior que zero.");
        }

        @Test
        void shouldRejectZeroSets() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 0, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Quantidade de séries deve ser maior que zero.");
        }

        @Test
        void shouldRejectNegativeSets() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, -1, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Quantidade de séries deve ser maior que zero.");
        }

        @Test
        void shouldRejectNonPositiveMinimumRepRange() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 0, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Mínimo de repetições desejadas deve ser maior do que zero.");
        }

        @Test
        void shouldRejectNonPositiveMaximumRepRange() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 0,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Máximo de repetições desejadas deve ser maior do que zero.");
        }

        @Test
        void shouldRejectMinimumRepRangeGreaterThanMaximum() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 13, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Mínimo de repetições não pode ser maior que o máximo.");
        }

        @Test
        void shouldRejectNonPositiveTargetLoad() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            BigDecimal.ZERO, "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Carga alvo desejada deve ser maior do que zero.");
        }

        @Test
        void shouldRejectBlankTargetLoadText() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), " ", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Alvo desejado não pode ser vazio.");
        }

        @Test
        void shouldRejectNonPositiveDuration() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 0, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Duração em minutos deve ser maior que zero.");
        }

        @Test
        void shouldRejectNonPositiveDistance() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, BigDecimal.ZERO,
                            "Moderada", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Distância em quilômetros deve ser maior que zero.");
        }

        @Test
        void shouldRejectBlankIntensity() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            " ", 90, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Intensidade não pode ser vazia.");
        }

        @Test
        void shouldRejectNonPositiveRestSeconds() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 0, "Priorizar a técnica", CREATED_AT))
                    .withMessage("Segundos de descanso deve ser maior do que zero.");
        }

        @Test
        void shouldRejectBlankNotes() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, " ", CREATED_AT))
                    .withMessage("Anotações não podem ser vazias.");
        }

        @Test
        void shouldRequireCreationDate() {
            assertThatExceptionOfType(InvalidWorkoutActivityException.class)
                    .isThrownBy(() -> registerValidActivity(WORKOUT_DAY_ID, EXERCISE_ID, 1, 4, 8, 12,
                            new BigDecimal("80.50"), "RPE 8", 45, new BigDecimal("5.50"),
                            "Moderada", 90, "Priorizar a técnica", null))
                    .withMessage("Data de criação é obrigatória.");
        }
    }

    private WorkoutActivity registerValidActivity(
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId,
            Integer orderIndex,
            Integer sets,
            Integer repRangeMin,
            Integer repRangeMax,
            BigDecimal targetLoadKg,
            String targetLoadText,
            Integer durationMinutes,
            BigDecimal distanceKm,
            String intensityText,
            Integer restSeconds,
            String notes,
            LocalDateTime createdAt
    ) {
        return WorkoutActivity.register(
                workoutDayId,
                exerciseId,
                orderIndex,
                sets,
                repRangeMin,
                repRangeMax,
                targetLoadKg,
                targetLoadText,
                durationMinutes,
                distanceKm,
                intensityText,
                restSeconds,
                notes,
                createdAt
        );
    }
}
