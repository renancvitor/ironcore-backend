package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record FatMassKg(Double value) {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    public FatMassKg {
        Objects.requireNonNull(value, "Massa gorda não pode ser nulo");

        if (!Double.isFinite(value) || value < 0 || value > MAX_EXPECTED_WEIGHT_KG) {
            throw new IllegalArgumentException("Massa gorda deve ser entre 0 e 500.");
        }
    }
}
