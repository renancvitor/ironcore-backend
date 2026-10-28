package com.ironcore.interfaces.rest.workoutsession.dto.analytics;

import java.util.List;

public record ExerciseStrengthEvolutionResponse(
        Long exerciseId,
        List<ExerciseStrengthEvolutionPointResponse> points
) {
}
