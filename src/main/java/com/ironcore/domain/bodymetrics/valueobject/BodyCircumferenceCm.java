package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;

public record BodyCircumferenceCm(Double value) {

    private static final double MAX_EXPECTED_CIRCUMFERENCE_CM = 300.0;

    public BodyCircumferenceCm {
        if (value == null) {
            throw new InvalidBodyMetricException("Circunferência corporal é obrigatória.");
        }

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_CIRCUMFERENCE_CM) {
            throw new InvalidBodyMetricException("Circunferência corporal está fora do intervalo esperado.");
        }
    }
}
