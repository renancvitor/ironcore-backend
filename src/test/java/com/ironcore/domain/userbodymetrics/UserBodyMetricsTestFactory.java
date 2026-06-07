package com.ironcore.domain.userbodymetrics;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.*;

public final class UserBodyMetricsTestFactory {

    private UserBodyMetricsTestFactory() {
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
}
