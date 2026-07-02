package com.ironcore.application.bodymetrics.latest;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.person.valueobject.PersonId;

import java.time.LocalDateTime;

public record GetLatestBodyMetricsResult(
        BodyMetricsId id,
        PersonId personId,
        LocalDateTime measuredAt,
        BodyWeightKg weight,
        BodyHeightCm height,
        BodyCircumferences circumferences,
        BMI bmi,
        BodyFatPercentage bodyFatPercentage,
        FatMassKg fatMassKg,
        LeanMassKg leanMassKg,
        String notes,
        LocalDateTime updatedAt
) {
}
