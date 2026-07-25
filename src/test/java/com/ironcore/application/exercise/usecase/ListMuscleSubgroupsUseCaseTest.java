package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.MuscleSubgroupItemResult;
import com.ironcore.application.exercise.catalog.usecase.ListMuscleSubgroupsUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMuscleSubgroupsUseCaseTest {

    @Mock
    private ExerciseFilterCatalogQueryPort queryPort;

    @InjectMocks
    private ListMuscleSubgroupsUseCase listMuscleSubgroupsUseCase;

    @Nested
    class WithoutMuscleGroupFilter {

        @Test
        void shouldListAllActiveMuscleSubgroupsWhenMuscleGroupIdIsNull() {
            List<MuscleSubgroupItemResult> expected = List.of(muscleSubgroupItemResult());
            when(queryPort.findActiveMuscleSubgroups()).thenReturn(expected);

            List<MuscleSubgroupItemResult> result = listMuscleSubgroupsUseCase.execute(null);

            verify(queryPort).findActiveMuscleSubgroups();
            verify(queryPort, never()).findActiveMuscleSubgroupsByMuscleGroupId(any());
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class WithMuscleGroupFilter {

        @Test
        void shouldListActiveMuscleSubgroupsByMuscleGroupId() {
            MuscleGroupId muscleGroupId = new MuscleGroupId(1L);
            List<MuscleSubgroupItemResult> expected = List.of(muscleSubgroupItemResult());
            when(queryPort.findActiveMuscleSubgroupsByMuscleGroupId(muscleGroupId)).thenReturn(expected);

            List<MuscleSubgroupItemResult> result = listMuscleSubgroupsUseCase.execute(muscleGroupId);

            verify(queryPort).findActiveMuscleSubgroupsByMuscleGroupId(muscleGroupId);
            verify(queryPort, never()).findActiveMuscleSubgroups();
            assertThat(result).isEqualTo(expected);
        }
    }

    private static MuscleSubgroupItemResult muscleSubgroupItemResult() {
        return new MuscleSubgroupItemResult(
                new MuscleSubgroupId(1L),
                new MuscleSubgroupCode("DELTOID"),
                new MuscleGroupId(1L),
                "Deltoide"
        );
    }
}
