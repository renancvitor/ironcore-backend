package com.ironcore.application.bodymetrics;

import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

public final class UpdateBodyMetricsUseCaseTestFactory {

    private static final BodyMetricsId USER_BODY_METRICS_ID = new BodyMetricsId(1L);
    private static final UserId USER_ID = new UserId(1L);

    private UpdateBodyMetricsUseCaseTestFactory() {
    }

    public static UpdateBodyMetricsCommand commandWithoutCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static UpdateBodyMetricsCommand commandWithCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
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

    public static UpdateBodyMetricsCommand commandWithMaleRequiredCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
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

    public static UpdateBodyMetricsCommand commandWithFemaleRequiredCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
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

    public static UpdateBodyMetricsCommand commandWithInsufficientMaleCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
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

    public static UpdateBodyMetricsCommand commandWithInsufficientFemaleCircumferences() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
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

    public static UpdateBodyMetricsCommand commandWithoutWeight() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                null,
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static UpdateBodyMetricsCommand commandWithoutHeight() {
        return new UpdateBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                new BodyWeightKg(65.0),
                null,
                null,
                "TEXT"
        );
    }
}
