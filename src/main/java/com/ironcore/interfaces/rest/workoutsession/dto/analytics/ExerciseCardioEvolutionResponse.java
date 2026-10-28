package com.ironcore.interfaces.rest.workoutsession.dto.analytics;

import java.util.List;

public record ExerciseCardioEvolutionResponse(
        Long exerciseId,
        List<ExerciseCardioEvolutionPointResponse> points,
        ExerciseCardioEvolutionSummaryResponse summary
) {
}
