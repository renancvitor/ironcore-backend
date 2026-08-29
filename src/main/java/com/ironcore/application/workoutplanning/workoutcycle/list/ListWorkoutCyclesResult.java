package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListWorkoutCyclesResult(
        PageResult<ListWorkoutCyclesItemResult> cycles
) {
}
