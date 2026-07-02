package com.ironcore.interfaces.rest.bodymetrics.dto.update;

import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesResponse;

import java.time.LocalDateTime;

public record UpdateBodyMetricsResponse(
        Long id,
        Long userId,
        LocalDateTime measuredAt,
        Double weightKg,
        Double heightCm,
        BodyCircumferencesResponse circumferences,
        Double bmi,
        Double bodyFatPercentage,
        Double fatMass,
        Double leanMass,
        String notes,
        LocalDateTime updatedAt
) {
}
