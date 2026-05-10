package com.ironcore.domain.userbodymetrics;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.userbodymetrics.valueobject.*;

import java.time.LocalDateTime;

public final class UserBodyMetricsTestFactory {

    private UserBodyMetricsTestFactory() {
    }

    public static User validUser() {
        return User.register(
                "Renan",
                new Email("renan@example.com"),
                new PasswordHash("hashed-password"),
                new Sex(SexType.MALE),
                LocalDateTime.of(2026, 5, 9, 10, 0));
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
