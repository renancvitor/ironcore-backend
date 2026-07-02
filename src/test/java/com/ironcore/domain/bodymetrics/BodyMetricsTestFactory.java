package com.ironcore.domain.bodymetrics;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;

import java.time.LocalDateTime;

public final class BodyMetricsTestFactory {

    private BodyMetricsTestFactory() {
    }

    public static UserId userId() {
        return new UserId(1L);
    }

    public static BodyHeightCm heightInCm(double value) {
        return new BodyHeightCm(value);
    }

    public static BodyWeightKg weightInKg(double value) {
        return new BodyWeightKg(value);
    }

    public static BodyCircumferences navyCircumferences(double neckCm, double waistCm) {
        return navyCircumferences(neckCm, waistCm, null);
    }

    public static BodyCircumferences navyCircumferences(double neckCm, double waistCm, Double hipCm) {
        return new BodyCircumferences(
                circumferenceInCm(neckCm),
                null,
                null,
                null,
                null,
                circumferenceInCm(waistCm),
                optionalCircumferenceInCm(hipCm),
                null,
                null);
    }

    public static BodyCircumferenceCm circumferenceInCm(double value) {
        return new BodyCircumferenceCm(value);
    }

    private static BodyCircumferenceCm optionalCircumferenceInCm(Double value) {
        return value == null ? null : circumferenceInCm(value);
    }

    public static BodyFatPercentage bodyFatPercentage(double value) {
        return new BodyFatPercentage(value);
    }

    public static FatMassKg fatMass(double value) {
        return new FatMassKg(value);
    }

    public static BodyMetrics restoreBodyMetrics() {
        return BodyMetrics.restore(
                new BodyMetricsId(1L),
                userId(),
                LocalDateTime.of(2026, 5, 10, 10, 0),
                weightInKg(80.0),
                heightInCm(180.0),
                navyCircumferences(40.0, 85.0),
                new BMI(24.69),
                bodyFatPercentage(18.0),
                fatMass(14.4),
                new LeanMassKg(65.6),
                LocalDateTime.of(2026, 5, 10, 11, 0),
                "Medição restaurada para teste."
        );
    }
}
