package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record BodyHeightCm(Double value) {

    private static final double MAX_EXPECTED_HEIGHT_CM = 300.0;

    public BodyHeightCm {
        Objects.requireNonNull(value, "Altura é obrigatória");

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_HEIGHT_CM) {
            throw new IllegalArgumentException("Altura está fora do intervalo esperado");
        }
    }

    public double inMeters() {
        return value / 100;
    }
}
