package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

import java.util.Objects;

public record BodyWeightKg(Double value) {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    public BodyWeightKg {
        if (value == null) {
            throw new InvalidBodyMetricException("Peso é obrigatório");
        }

        if (!Double.isFinite(value) || value <= 0 || value > MAX_EXPECTED_WEIGHT_KG) {
            throw new InvalidBodyMetricException("Peso está fora do intervalo esperado");
        }
    }
}
