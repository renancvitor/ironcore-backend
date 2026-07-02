package com.ironcore.infrastructure.persistence.bodymetrics;

import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;

import java.time.LocalDateTime;

import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;

public final class BodyMetricsTestFactory {

    public static final LocalDateTime MEASURED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);

    private BodyMetricsTestFactory() {
    }

    public static BodyMetricsEntity createUserBodyMetricsEntity() {
        return createUserBodyMetricsEntity(1L);
    }

    private  static BodyMetricsEntity createUserBodyMetricsEntity(Long id) {
        return new BodyMetricsEntity(
                id,
                userEntity(),
                MEASURED_AT,
                65.0,
                1.67,
                20.0,
                120.0,
                150.0,
                36.0,
                20.0,
                60.0,
                60.0,
                20.0,
                20.0,
                50.0,
                10.0,
                10.0,
                55.0,
                UPDATED_AT,
                "TEXT"
        );
    }
}
