package com.ironcore.domain.exercise.model;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ExerciseTest {

    private static final ExerciseId ID = new ExerciseId(1L);
    private static final EquipmentTypeId EQUIPMENT_TYPE_ID = new EquipmentTypeId(1L);
    private static final ActivityTypeId ACTIVITY_TYPE_ID = new ActivityTypeId(1L);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 12, 10, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 7, 12, 11, 0);

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingExercise() {
            Exercise exercise = restoreExercise();

            assertThat(exercise.getId()).isEqualTo(new ExerciseId(1L));
            assertThat(exercise.getName()).isEqualTo("Supino reto");
            assertThat(exercise.getEquipmentTypeId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(exercise.getActivityTypeId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(exercise.getUnilateral()).isFalse();
            assertThat(exercise.getCompound()).isTrue();
            assertThat(exercise.getSuggestedRestSeconds()).isEqualTo(90);
            assertThat(exercise.getActive()).isTrue();
            assertThat(exercise.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(exercise.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldRestoreExerciseWithoutSuggestedRestSeconds() {
            Exercise exercise = Exercise.restore(
                    ID,
                    "Supino reto",
                    EQUIPMENT_TYPE_ID,
                    ACTIVITY_TYPE_ID,
                    false,
                    true,
                    null,
                    true,
                    CREATED_AT,
                    UPDATED_AT
            );

            assertThat(exercise.getSuggestedRestSeconds()).isNull();
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankName() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            " ",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Nome não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            null,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id não pode ser nulo.");
        }

        @Test
        void shouldRequireEquipmentTypeIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            null,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id do tipo de equipamento não pode ser nulo.");
        }

        @Test
        void shouldRequireActivityTypeIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            null,
                            false,
                            true,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id do tipo de atividade não pode ser nulo.");
        }

        @Test
        void shouldRequireUnilateralTagWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            null,
                            true,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Tag de exercício unilateral não pode ser nulo.");
        }

        @Test
        void shouldRequireCompoundTagWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            null,
                            90,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Tag de exercício composto não pode ser nulo.");
        }

        @Test
        void shouldRejectZeroSuggestedRestSeconds() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            0,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Descanso sugerido deve ser positivo.");
        }

        @Test
        void shouldRejectNegativeSuggestedRestSeconds() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            -1,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Descanso sugerido deve ser positivo.");
        }

        @Test
        void shouldRequireActiveTagWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            90,
                            null,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Tag de exercício ativo não pode ser nulo.");
        }

        @Test
        void shouldRequireCreatedAtWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> Exercise.restore(
                            ID,
                            "Supino reto",
                            EQUIPMENT_TYPE_ID,
                            ACTIVITY_TYPE_ID,
                            false,
                            true,
                            90,
                            true,
                            null,
                            UPDATED_AT
                    ))
                    .withMessage("Data de criação não pode ser nulo.");
        }
    }
}
