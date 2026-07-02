package com.ironcore.domain.bodymetrics.service;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.FatMassKg;

public class FatMassCalculator {

    public FatMassKg calculate(BodyMetrics metrics) {
        requireNonNull(metrics, "Medidas corporais são obrigatórias.");
        return calculate(metrics.getWeight(), metrics.getBodyFatPercentage());
    }

    public FatMassKg calculate(BodyWeightKg weight, BodyFatPercentage bodyFatPercentage) {
        requireNonNull(weight, "Peso é obrigatório.");
        requireNonNull(bodyFatPercentage, "Percentual de gordura é obrigatório.");

        double result = (weight.value() * bodyFatPercentage.value()) / 100;

        return new FatMassKg(result);
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
