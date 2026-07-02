package com.ironcore.domain.bodymetrics.service;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.FatMassKg;
import com.ironcore.domain.bodymetrics.valueobject.LeanMassKg;

public class LeanMassCalculator {

    public LeanMassKg calculate(BodyMetrics metrics) {
        requireNonNull(metrics, "Medidas corporais são obrigatórias.");
        return calculate(metrics.getWeight(), metrics.getFatMassKg());
    }

    public LeanMassKg calculate(BodyWeightKg weight, FatMassKg fatMass) {
        requireNonNull(weight, "Peso é obrigatório.");
        requireNonNull(fatMass, "Massa gorda é obrigatória.");

        double result = weight.value() - fatMass.value();

        return new LeanMassKg(result);
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
