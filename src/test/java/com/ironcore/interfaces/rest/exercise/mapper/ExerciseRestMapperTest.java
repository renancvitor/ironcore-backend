package com.ironcore.interfaces.rest.exercise.mapper;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.application.exercise.usecase.ExerciseMuscleTargetItemResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.interfaces.rest.exercise.dto.GetExerciseByIdResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseRestMapperTest {

    @Nested
    class ToResponse {

        @Test
        void shouldMapExerciseDetailFields() {
            GetExerciseByIdResult result = getExerciseByIdResult();

            GetExerciseByIdResponse response = ExerciseRestMapper.toResponse(result);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Supino reto");
            assertThat(response.equipmentType().id()).isEqualTo(2L);
            assertThat(response.equipmentType().code()).isEqualTo("BARBELL");
            assertThat(response.equipmentType().name()).isEqualTo("Barra");
            assertThat(response.activityType().id()).isEqualTo(3L);
            assertThat(response.activityType().code()).isEqualTo("STRENGTH");
            assertThat(response.activityType().name()).isEqualTo("Força");
            assertThat(response.unilateral()).isFalse();
            assertThat(response.compound()).isTrue();
            assertThat(response.suggestedRestSeconds()).isEqualTo(90);
            assertThat(response.active()).isTrue();
            assertThat(response.muscleTargets()).singleElement().satisfies(muscleTarget -> {
                assertThat(muscleTarget.muscleSubgroup().id()).isEqualTo(4L);
                assertThat(muscleTarget.muscleSubgroup().code()).isEqualTo("PECTORALIS_MAJOR");
                assertThat(muscleTarget.muscleSubgroup().muscleGroupId()).isEqualTo(5L);
                assertThat(muscleTarget.muscleSubgroup().name()).isEqualTo("Peitoral maior");
                assertThat(muscleTarget.targetRole()).isEqualTo(TargetRoleType.PRIMARY);
            });
        }
    }

    private static GetExerciseByIdResult getExerciseByIdResult() {
        return new GetExerciseByIdResult(
                new ExerciseId(1L),
                "Supino reto",
                new EquipmentTypeItemResult(
                        new EquipmentTypeId(2L),
                        new EquipmentTypeCode("BARBELL"),
                        "Barra"
                ),
                new ActivityTypeItemResult(
                        new ActivityTypeId(3L),
                        new ActivityTypeCode("STRENGTH"),
                        "Força"
                ),
                false,
                true,
                90,
                true,
                List.of(new ExerciseMuscleTargetItemResult(
                        new MuscleSubgroupItemResult(
                                new MuscleSubgroupId(4L),
                                new MuscleSubgroupCode("PECTORALIS_MAJOR"),
                                new MuscleGroupId(5L),
                                "Peitoral maior"
                        ),
                        TargetRoleType.PRIMARY
                ))
        );
    }
}
