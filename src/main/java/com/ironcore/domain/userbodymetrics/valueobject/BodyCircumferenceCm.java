package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record BodyCircumferenceCm(Double value) {

    private static final double MAX_EXPECTED_CIRCUMFERENCE_CM = 300.0;

    public BodyCircumferenceCm {
        Objects.requireNonNull(value, "Circunferência corporal é obrigatória");

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_CIRCUMFERENCE_CM) {
            throw new IllegalArgumentException("Circunferência corporal está fora do intervalo esperado");
        }
    }
}
