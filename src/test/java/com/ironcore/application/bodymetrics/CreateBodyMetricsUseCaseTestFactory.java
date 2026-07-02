package com.ironcore.application.bodymetrics;

import com.ironcore.application.bodymetrics.create.CreateBodyMetricsCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;

public final class CreateBodyMetricsUseCaseTestFactory {

    private CreateBodyMetricsUseCaseTestFactory() {
    }

    public static CreateBodyMetricsCommand commandWithoutCircumferences() {
        return new CreateBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static CreateBodyMetricsCommand commandWithCircumferences() {
        return new CreateBodyMetricsCommand(
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

    public static CreateBodyMetricsCommand commandWithMaleRequiredCircumferences() {
        return new CreateBodyMetricsCommand(
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

    public static CreateBodyMetricsCommand commandWithFemaleRequiredCircumferences() {
        return new CreateBodyMetricsCommand(
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

    public static CreateBodyMetricsCommand commandWithInsufficientMaleCircumferences() {
        return new CreateBodyMetricsCommand(
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

    public static CreateBodyMetricsCommand commandWithInsufficientFemaleCircumferences() {
        return new CreateBodyMetricsCommand(
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

    public static CreateBodyMetricsCommand commandWithoutWeight() {
        return new CreateBodyMetricsCommand(
                new UserId(1L),
                null,
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static CreateBodyMetricsCommand commandWithoutHeight() {
        return new CreateBodyMetricsCommand(
                new UserId(1L),
                new BodyWeightKg(65.0),
                null,
                null,
                "TEXT"
        );
    }
}
