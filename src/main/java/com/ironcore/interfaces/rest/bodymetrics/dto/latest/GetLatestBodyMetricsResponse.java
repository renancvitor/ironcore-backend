package com.ironcore.interfaces.rest.bodymetrics.dto.latest;

import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesResponse;

import java.time.LocalDateTime;

public record GetLatestBodyMetricsResponse(
        Long id,
        Long personId,
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
