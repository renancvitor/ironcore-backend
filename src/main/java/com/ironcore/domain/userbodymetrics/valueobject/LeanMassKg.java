package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record LeanMassKg(Double value) {

    public LeanMassKg {
        Objects.requireNonNull(value, "Massa magra não pode ser nulo");

        if (value().isInfinite() || value < 0 || value > 100) {
            throw new IllegalArgumentException("Massa magra deve ser entre 0 e 100.");
        }
    }
}
