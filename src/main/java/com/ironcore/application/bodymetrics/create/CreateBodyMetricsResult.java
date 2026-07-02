package com.ironcore.application.bodymetrics.create;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.person.valueobject.PersonId;

import java.time.LocalDateTime;

public record CreateBodyMetricsResult(
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
        String notes
) {
}
