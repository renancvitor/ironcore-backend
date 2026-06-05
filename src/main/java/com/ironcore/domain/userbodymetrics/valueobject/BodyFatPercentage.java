package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

public record BodyFatPercentage(Double value) {

    public BodyFatPercentage {
        if (value == null) {
            throw new InvalidBodyMetricException("Percentual de gordura corporal é obrigatório.");
        }

        if (!Double.isFinite(value) || value < 0 || value > 100) {
            throw new InvalidBodyMetricException("Percentual de gordura corporal deve estar entre 0 e 100.");
        }
    }
}
