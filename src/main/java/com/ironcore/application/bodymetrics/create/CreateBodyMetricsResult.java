package com.ironcore.application.bodymetrics.create;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDateTime;

public record CreateBodyMetricsResult(
        BodyMetricsId id,
        UserId userId,
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
