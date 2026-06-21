package com.ironcore.application.userbodymetrics.get;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.*;

import java.time.LocalDateTime;

public record GetUserBodyMetricsResult(
        UserBodyMetricsId id,
        UserId userId,
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
