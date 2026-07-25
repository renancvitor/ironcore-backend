package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.usecase.ListMuscleGroupsUseCase;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
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
class ListMuscleGroupsUseCaseTest {

    @Mock
    private ExerciseFilterCatalogQueryPort queryPort;

    @InjectMocks
    private ListMuscleGroupsUseCase listMuscleGroupsUseCase;

    @Test
    void shouldListActiveMuscleGroups() {
        List<MuscleGroupItemResult> expected = List.of(new MuscleGroupItemResult(
                new MuscleGroupId(1L),
                new MuscleGroupCode("BACK"),
                "Costas"
        ));
        when(queryPort.findActiveMuscleGroups()).thenReturn(expected);

        List<MuscleGroupItemResult> result = listMuscleGroupsUseCase.execute();

        verify(queryPort).findActiveMuscleGroups();
        assertThat(result).isEqualTo(expected);
    }
}
