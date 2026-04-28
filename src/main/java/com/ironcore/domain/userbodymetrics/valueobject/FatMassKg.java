package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record FatMassKg(Double value) {

    public FatMassKg {
        Objects.requireNonNull(value, "Massa gorda não pode ser nulo");

        if (Double.isInfinite(value) || value < 0 || value > 100) {
            throw new IllegalArgumentException("Massa gorda deve ser entre 0 e 100.");
        }
    }
}
