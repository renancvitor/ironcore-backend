package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

public record LeanMassKg(Double value) {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    public LeanMassKg {
        if (value == null) {
            throw new InvalidBodyMetricException("Massa magra é obrigatória.");
        }

        if (!Double.isFinite(value) || value < 0 || value > MAX_EXPECTED_WEIGHT_KG) {
            throw new InvalidBodyMetricException("Massa magra deve estar entre 0 e 500.");
        }
    }
}
