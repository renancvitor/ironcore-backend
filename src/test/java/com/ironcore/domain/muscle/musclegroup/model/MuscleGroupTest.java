package com.ironcore.domain.muscle.musclegroup.model;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclegroup.MuscleGroupTestFactory.restoreMuscleGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MuscleGroupTest {

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingMuscleGroup() {
            MuscleGroup muscleGroup = restoreMuscleGroup();

            assertThat(muscleGroup.getId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(muscleGroup.getCode()).isEqualTo(new MuscleGroupCode("BACK"));
            assertThat(muscleGroup.getDisplayName()).isEqualTo("Costas");
            assertThat(muscleGroup.getActive()).isTrue();
            assertThat(muscleGroup.getSortOrder()).isEqualTo(20);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankDisplayName() {
            assertThatExceptionOfType(InvalidMuscleGroupException.class)
                    .isThrownBy(() -> MuscleGroup.restore(
                            new MuscleGroupId(1L),
                            new MuscleGroupCode("BACK"),
                            " ",
                            true,
                            20
                    ))
                    .withMessage("Nome de exibição não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidMuscleGroupException.class)
                    .isThrownBy(() -> MuscleGroup.restore(
                            null,
                            new MuscleGroupCode("BACK"),
                            "Costas",
                            true,
                            20
                    ))
                    .withMessage("Id não pode ser nulo.");
        }
    }
}
