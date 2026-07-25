package com.ironcore.application.exercise.catalog.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.MuscleSubgroupItemResult;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMuscleSubgroupsUseCase {

    private final ExerciseFilterCatalogQueryPort queryPort;

    @Transactional(readOnly = true)
    public List<MuscleSubgroupItemResult> execute(MuscleGroupId muscleGroupId) {
        if (muscleGroupId == null) {
            return queryPort.findActiveMuscleSubgroups();
        }

        return queryPort.findActiveMuscleSubgroupsByMuscleGroupId(muscleGroupId);
    }
}
