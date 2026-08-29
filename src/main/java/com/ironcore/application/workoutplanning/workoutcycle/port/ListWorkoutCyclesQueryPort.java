package com.ironcore.application.workoutplanning.workoutcycle.port;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesItemResult;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;

public interface ListWorkoutCyclesQueryPort {

    PageResult<ListWorkoutCyclesItemResult> findWorkoutCycles(
            PersonId personId,
            WorkoutStatus workoutStatus,
            TrainingGoalId trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            String name,
            PageQuery pageQuery
    );
}
