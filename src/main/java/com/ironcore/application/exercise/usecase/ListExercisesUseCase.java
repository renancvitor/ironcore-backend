package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.port.ListExercisesQueryPort;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListExercisesUseCase {

    private final ListExercisesQueryPort queryPort;

    @Transactional(readOnly = true)
    public ListExercisesResult execute(ListExercisesCommand command) {
        PageQuery pageQuery = new PageQuery(
                command.page(),
                command.size()
        );

        PageResult<ListExercisesItemResult> exercises =
                queryPort.findActiveExercises(
                        command.name(),
                        command.activityTypeId(),
                        command.equipmentTypeId(),
                        command.muscleGroupId(),
                        command.muscleSubgroupId(),
                        command.targetRole(),
                        pageQuery
                );

        return new ListExercisesResult(exercises);
    }
}
