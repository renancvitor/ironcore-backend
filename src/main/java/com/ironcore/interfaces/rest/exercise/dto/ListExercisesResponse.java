package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.application.shared.pagination.PageResult;

public record ListExercisesResponse(
        PageResult<ListExercisesItemResponse> exercises
) {
}
