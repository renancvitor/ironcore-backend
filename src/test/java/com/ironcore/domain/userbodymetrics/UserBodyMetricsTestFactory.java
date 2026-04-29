package com.ironcore.domain.userbodymetrics;

import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;

public final class UserBodyMetricsTestFactory {

    private UserBodyMetricsTestFactory() {
    }

    public static BodyHeightCm heightInCm(double value) {
        return new BodyHeightCm(value);
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
}
