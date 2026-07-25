package com.ironcore.application.exercise.catalog.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.MuscleGroupItemResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMuscleGroupsUseCase {

    private final ExerciseFilterCatalogQueryPort queryPort;

    @Transactional(readOnly = true)
    public List<MuscleGroupItemResult> execute() {
        return queryPort.findActiveMuscleGroups();
    }
}
