package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

public record BMI(Double value) {

    public BMI {
        if (value == null) {
            throw new InvalidBodyMetricException("IMC não pode ser nulo.");
        }

        if (!Double.isFinite(value) || value < 0 || value > 300) {
            throw new InvalidBodyMetricException("IMC deve ser entre 0 e 300");
        }
    }
}
