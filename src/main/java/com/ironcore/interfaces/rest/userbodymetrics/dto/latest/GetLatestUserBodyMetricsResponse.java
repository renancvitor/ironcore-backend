package com.ironcore.interfaces.rest.userbodymetrics.dto.latest;

import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesResponse;

import java.time.LocalDateTime;

public record GetLatestUserBodyMetricsResponse(
        Long id,
        Long userId,
        LocalDateTime measuredAt,
        Double weightKg,
        Double heightCm,
        BodyCircumferencesResponse circumferences,
        Double bmi,
        Double bodyFatPercentage,
        Double fatMassKg,
        Double leanMassKg,
        String notes,
        LocalDateTime updatedAt
) {
}
