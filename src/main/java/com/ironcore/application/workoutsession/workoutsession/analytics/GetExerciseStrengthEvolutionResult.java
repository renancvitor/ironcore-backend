package com.ironcore.application.workoutsession.workoutsession.analytics;

import java.util.List;

public record GetExerciseStrengthEvolutionResult(
        Long exerciseId,
        List<ExerciseStrengthEvolutionPointResult> points
) {
}
