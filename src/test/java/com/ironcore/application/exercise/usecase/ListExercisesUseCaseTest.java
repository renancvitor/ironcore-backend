package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.port.ListExercisesQueryPort;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListExercisesUseCaseTest {

    @Mock
    private ListExercisesQueryPort queryPort;

    @InjectMocks
    private ListExercisesUseCase listExercisesUseCase;

    @Test
    void shouldListExercisesWithFiltersAndPagination() {
        ListExercisesCommand command = new ListExercisesCommand(
                "supino",
                new ActivityTypeId(1L),
                new EquipmentTypeId(2L),
                new MuscleGroupId(3L),
                new MuscleSubgroupId(4L),
                TargetRoleType.PRIMARY,
                1,
                2
        );
        PageQuery pageQuery = new PageQuery(1, 2);
        PageResult<ListExercisesItemResult> expectedPage = new PageResult<>(
                List.of(listExerciseItemResult()),
                1,
                2,
                5,
                3,
                false
        );
        when(queryPort.findActiveExercises(
                command.name(),
                command.activityTypeId(),
                command.equipmentTypeId(),
                command.muscleGroupId(),
                command.muscleSubgroupId(),
                command.targetRole(),
                pageQuery
        )).thenReturn(expectedPage);

        ListExercisesResult result = listExercisesUseCase.execute(command);

        verify(queryPort).findActiveExercises(
                command.name(),
                command.activityTypeId(),
                command.equipmentTypeId(),
                command.muscleGroupId(),
                command.muscleSubgroupId(),
                command.targetRole(),
                pageQuery
        );
        assertThat(result.exercises()).isEqualTo(expectedPage);
    }

    private static ListExercisesItemResult listExerciseItemResult() {
        return new ListExercisesItemResult(
                new ExerciseId(1L),
                "Supino reto",
                new EquipmentTypeItemResult(
                        new EquipmentTypeId(2L),
                        new EquipmentTypeCode("BARBELL"),
                        "Barra"
                ),
                new ActivityTypeItemResult(
                        new ActivityTypeId(1L),
                        new ActivityTypeCode("STRENGTH"),
                        "Força"
                ),
                false,
                true,
                90
        );
    }
}
