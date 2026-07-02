package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;

public record BMI(Double value) {

    public BMI {
        if (value == null) {
            throw new InvalidBodyMetricException("IMC é obrigatório.");
        }

        if (!Double.isFinite(value) || value < 0 || value > 300) {
            throw new InvalidBodyMetricException("IMC deve estar entre 0 e 300.");
        }
    }
}
