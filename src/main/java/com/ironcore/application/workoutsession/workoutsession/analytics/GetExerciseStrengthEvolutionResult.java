package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.List;

public record GetExerciseStrengthEvolutionResult(
        ExerciseId exerciseId,
        List<ExerciseStrengthEvolutionPointResult> points
) {
}
