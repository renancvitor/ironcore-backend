package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record BodyFatPercentage(Double value) {

    public BodyFatPercentage {
        Objects.requireNonNull(value, "Percentual de gordura corporal não pode ser nulo");

        if (!Double.isFinite(value) || value < 0 || value > 100) {
            throw new IllegalArgumentException("Percentual de gordura corporal deve ser entre 0 e 100");
        }
    }
}
