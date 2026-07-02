package com.ironcore.domain.bodymetrics.service;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BMI;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;

public class BMICalculator {

    public BMI calculate(BodyMetrics metrics) {
        requireNonNull(metrics, "Medidas corporais são obrigatórias.");
        return calculate(metrics.getHeight(), metrics.getWeight());
    }

    public BMI calculate(BodyHeightCm height, BodyWeightKg weight) {
        requireNonNull(height, "Altura é obrigatória.");
        requireNonNull(weight, "Peso é obrigatório.");

        double result = weight.value() / (height.inMeters() * height.inMeters());

        return new BMI(result);
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
