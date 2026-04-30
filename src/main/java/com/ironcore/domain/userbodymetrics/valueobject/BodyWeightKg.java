package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record BodyWeightKg(Double value) {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    public BodyWeightKg {
        Objects.requireNonNull(value, "Peso é obrigatório");

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_WEIGHT_KG) {
            throw new IllegalArgumentException("Peso está fora do intervalo esperado");
        }
    }
}
