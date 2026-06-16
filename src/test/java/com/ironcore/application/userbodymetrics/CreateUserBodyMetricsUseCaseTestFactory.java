package com.ironcore.application.userbodymetrics;

import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;

public final class CreateUserBodyMetricsUseCaseTestFactory {

    private CreateUserBodyMetricsUseCaseTestFactory() {
    }

    public static CreateUserBodyMetricsCommand commandWithoutCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(20.0),
                        new BodyCircumferenceCm(150.0),
                        new BodyCircumferenceCm(180.0),
                        new BodyCircumferenceCm(36.0),
                        new BodyCircumferenceCm(32.0),
                        new BodyCircumferenceCm(60.0),
                        new BodyCircumferenceCm(70.0),
                        new BodyCircumferenceCm(40.0),
                        new BodyCircumferenceCm(30.0)
                ),
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithMaleRequiredCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(38.0),
                        null,
                        null,
                        null,
                        null,
                        new BodyCircumferenceCm(82.0),
                        null,
                        null,
                        null
                ),
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithFemaleRequiredCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(32.0),
                        null,
                        null,
                        null,
                        null,
                        new BodyCircumferenceCm(74.0),
                        new BodyCircumferenceCm(96.0),
                        null,
                        null
                ),
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithInsufficientMaleCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(38.0),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithInsufficientFemaleCircumferences() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(32.0),
                        null,
                        null,
                        null,
                        null,
                        new BodyCircumferenceCm(74.0),
                        null,
                        null,
                        null
                ),
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithoutWeight() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                null,
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static CreateUserBodyMetricsCommand commandWithoutHeight() {
        return new CreateUserBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                null,
                null,
                "TEXT"
        );
    }
}
