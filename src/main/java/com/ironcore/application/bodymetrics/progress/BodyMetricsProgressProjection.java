package com.ironcore.application.bodymetrics.progress;

import java.time.LocalDateTime;

public record BodyMetricsProgressProjection(
        LocalDateTime measuredAt,

        Double weightKg,
        Double fatMassKg,
        Double leanMassKg,

        Double bodyFatPercentage,

        Double bmi,

        Double neckCm,
        Double chestCm,
        Double shoulderCm,
        Double armCm,
        Double forearmCm,
        Double waistCm,
        Double hipCm,
        Double thighCm,
        Double calfCm
) {
}
