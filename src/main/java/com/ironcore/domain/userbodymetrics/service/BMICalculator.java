package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BMI;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;

public class BMICalculator {

    public BMI calculate(UserBodyMetrics metrics) {
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
