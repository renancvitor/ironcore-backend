package com.ironcore.application.exercise.usecase;

import com.ironcore.application.shared.pagination.PageResult;

public record ListExercisesResult(
        PageResult<ListExercisesItemResult> exercises
) {
}
