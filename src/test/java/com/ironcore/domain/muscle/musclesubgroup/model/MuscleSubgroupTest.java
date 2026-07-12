package com.ironcore.domain.muscle.musclesubgroup.model;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclesubgroup.MuscleSubgroupTestFactory.restoreMuscleSubgroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MuscleSubgroupTest {

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingMuscleSubgroup() {
            MuscleSubgroup muscleSubgroup = restoreMuscleSubgroup();

            assertThat(muscleSubgroup.getId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(muscleSubgroup.getMuscleGroupId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(muscleSubgroup.getCode()).isEqualTo(new MuscleSubgroupCode("DELTOID"));
            assertThat(muscleSubgroup.getDisplayName()).isEqualTo("Deltoide");
            assertThat(muscleSubgroup.getActive()).isTrue();
            assertThat(muscleSubgroup.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankDisplayName() {
            assertThatExceptionOfType(InvalidMuscleSubgroupException.class)
                    .isThrownBy(() -> MuscleSubgroup.restore(
                            new MuscleSubgroupId(1L),
                            new MuscleGroupId(1L),
                            new MuscleSubgroupCode("DELTOID"),
                            " ",
                            true,
                            20
                    ))
                    .withMessage("Nome de exibição não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidMuscleSubgroupException.class)
                    .isThrownBy(() -> MuscleSubgroup.restore(
                            null,
                            new MuscleGroupId(1L),
                            new MuscleSubgroupCode("DELTOID"),
                            "Costas",
                            true,
                            20
                    ))
                    .withMessage("Id não pode ser nulo.");
        }
    }
}
