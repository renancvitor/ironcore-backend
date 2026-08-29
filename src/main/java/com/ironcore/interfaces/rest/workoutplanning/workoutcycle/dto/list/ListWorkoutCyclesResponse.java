package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListWorkoutCyclesResponse(
        PageResult<ListWorkoutCyclesItemResponse> cycles
) {
}
