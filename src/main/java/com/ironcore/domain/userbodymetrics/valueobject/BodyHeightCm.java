package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

public record BodyHeightCm(Double value) {

    private static final double MAX_EXPECTED_HEIGHT_CM = 300.0;

    public BodyHeightCm {
        if (value == null) {
            throw new InvalidBodyMetricException("Altura é obrigatória.");
        }

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_HEIGHT_CM) {
            throw new InvalidBodyMetricException("Altura está fora do intervalo esperado.");
        }
    }

    public double inMeters() {
        return value / 100;
    }
}
