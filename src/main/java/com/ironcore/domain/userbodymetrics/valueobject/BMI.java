package com.ironcore.domain.userbodymetrics.valueobject;

import java.util.Objects;

public record BMI(Double value) {

    public BMI {
        Objects.requireNonNull(value, "IMC não pode ser nulo.");

        if (Double.isInfinite(value) || value < 0 || value > 300) {
            throw new IllegalArgumentException("IMC deve ser entre 0 e 300");
        }
    }
}
