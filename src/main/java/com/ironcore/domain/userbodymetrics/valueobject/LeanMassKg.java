package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record LeanMassKg(Double value) {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    public LeanMassKg {
        Objects.requireNonNull(value, "Massa magra não pode ser nulo");

        if (!Double.isFinite(value) || value < 0 || value > MAX_EXPECTED_WEIGHT_KG) {
            throw new IllegalArgumentException("Massa magra deve ser entre 0 e 500.");
        }
    }
}
