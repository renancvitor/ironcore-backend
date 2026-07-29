package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.application.exercise.port.GetExerciseByIdQueryPort;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExerciseByIdUseCaseTest {

    @Mock
    private GetExerciseByIdQueryPort queryPort;

    @InjectMocks
    private GetExerciseByIdUseCase getExerciseByIdUseCase;

    @Nested
    class ExistingExercise {

        @Test
        void shouldReturnExerciseDetailById() {
            ExerciseId exerciseId = new ExerciseId(1L);
            GetExerciseByIdResult expected = getExerciseByIdResult();
            when(queryPort.findActiveDetailById(exerciseId)).thenReturn(Optional.of(expected));

            GetExerciseByIdResult result = getExerciseByIdUseCase.execute(exerciseId);

            verify(queryPort).findActiveDetailById(exerciseId);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class MissingExercise {

        @Test
        void shouldThrowResourceNotFoundWhenExerciseDoesNotExist() {
            ExerciseId exerciseId = new ExerciseId(99L);
            when(queryPort.findActiveDetailById(exerciseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getExerciseByIdUseCase.execute(exerciseId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Exercício não encontrado.");

            verify(queryPort).findActiveDetailById(exerciseId);
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
