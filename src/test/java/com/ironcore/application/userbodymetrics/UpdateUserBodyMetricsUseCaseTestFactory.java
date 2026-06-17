package com.ironcore.application.userbodymetrics;

import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

public final class UpdateUserBodyMetricsUseCaseTestFactory {

    private static final UserBodyMetricsId USER_BODY_METRICS_ID = new UserBodyMetricsId(1L);
    private static final UserId USER_ID = new UserId(1L);

    private UpdateUserBodyMetricsUseCaseTestFactory() {
    }

    public static UpdateUserBodyMetricsCommand commandWithoutCircumferences() {
        return new UpdateUserBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static UpdateUserBodyMetricsCommand commandWithCircumferences() {
        return new UpdateUserBodyMetricsCommand(
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

    public static UpdateUserBodyMetricsCommand commandWithMaleRequiredCircumferences() {
        return new UpdateUserBodyMetricsCommand(
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

    public static UpdateUserBodyMetricsCommand commandWithFemaleRequiredCircumferences() {
        return new UpdateUserBodyMetricsCommand(
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

    public static UpdateUserBodyMetricsCommand commandWithInsufficientMaleCircumferences() {
        return new UpdateUserBodyMetricsCommand(
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

    public static UpdateUserBodyMetricsCommand commandWithInsufficientFemaleCircumferences() {
        return new UpdateUserBodyMetricsCommand(
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

    public static UpdateUserBodyMetricsCommand commandWithoutWeight() {
        return new UpdateUserBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                null,
                new BodyHeightCm(167.0),
                null,
                "TEXT"
        );
    }

    public static UpdateUserBodyMetricsCommand commandWithoutHeight() {
        return new UpdateUserBodyMetricsCommand(
                USER_BODY_METRICS_ID,
                USER_ID,
                new BodyWeightKg(65.0),
                null,
                null,
                "TEXT"
        );
    }
}
