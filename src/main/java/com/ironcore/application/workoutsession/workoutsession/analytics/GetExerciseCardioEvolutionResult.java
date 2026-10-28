package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutsession.analytics.valueobject.CardioExecutionMetrics;

import java.util.List;

public record GetExerciseCardioEvolutionResult(
        ExerciseId exerciseId,
        List<ExerciseCardioEvolutionPointResult> points,
        CardioExecutionMetrics summary
) {
}
