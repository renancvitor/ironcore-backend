package com.ironcore.domain.exercisemuscletarget;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.exercisemuscletarget.exception.InvalidExerciseMuscleTargetException;
import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ironcore.domain.exercisemuscletarget.ExerciseMuscleTargetTestFactory.restoreExerciseMuscleTarget;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ExerciseMuscleTargetTest {

    private static final ExerciseMuscleTargetId ID = new ExerciseMuscleTargetId(1L);
    private static final ExerciseId EXERCISE_ID = new ExerciseId(1L);
    private static final MuscleSubgroupId MUSCLE_SUBGROUP_ID = new MuscleSubgroupId(1L);
    private static final TargetRoleType TARGET_ROLE = TargetRoleType.PRIMARY;
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 12, 10, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 7, 12, 11, 0);

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingExerciseMuscleTarget() {
            ExerciseMuscleTarget exerciseMuscleTarget = restoreExerciseMuscleTarget();

            assertThat(exerciseMuscleTarget.getId()).isEqualTo(new ExerciseMuscleTargetId(1L));
            assertThat(exerciseMuscleTarget.getExerciseId()).isEqualTo(new ExerciseId(1L));
            assertThat(exerciseMuscleTarget.getMuscleSubgroupId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(exerciseMuscleTarget.getTargetRole()).isEqualTo(TargetRoleType.PRIMARY);
            assertThat(exerciseMuscleTarget.getActive()).isTrue();
            assertThat(exerciseMuscleTarget.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(exerciseMuscleTarget.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            null,
                            EXERCISE_ID,
                            MUSCLE_SUBGROUP_ID,
                            TARGET_ROLE,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id não pode ser nulo.");
        }

        @Test
        void shouldRequireExerciseIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            ID,
                            null,
                            MUSCLE_SUBGROUP_ID,
                            TARGET_ROLE,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id do exercício não pode ser nulo.");
        }

        @Test
        void shouldRequireMuscleSubgroupIdWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            ID,
                            EXERCISE_ID,
                            null,
                            TARGET_ROLE,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Id do subgrupo muscular não pode ser nulo.");
        }

        @Test
        void shouldRequireTargetRoleWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            ID,
                            EXERCISE_ID,
                            MUSCLE_SUBGROUP_ID,
                            null,
                            true,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Papel do músculo alvo do exercício não pode ser nulo.");
        }

        @Test
        void shouldRequireActiveTagWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            ID,
                            EXERCISE_ID,
                            MUSCLE_SUBGROUP_ID,
                            TARGET_ROLE,
                            null,
                            CREATED_AT,
                            UPDATED_AT
                    ))
                    .withMessage("Tag de músculo alvo do exercício ativo não pode ser nulo.");
        }

        @Test
        void shouldRequireCreatedAtWhenRestoring() {
            assertThatExceptionOfType(InvalidExerciseMuscleTargetException.class)
                    .isThrownBy(() -> ExerciseMuscleTarget.restore(
                            ID,
                            EXERCISE_ID,
                            MUSCLE_SUBGROUP_ID,
                            TARGET_ROLE,
                            true,
                            null,
                            UPDATED_AT
                    ))
                    .withMessage("Data de criação não pode ser nulo.");
        }
    }
}
