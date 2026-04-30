package com.ironcore.domain.userbodymetrics.service;

import java.util.Objects;

import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BMI;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;

public class BMICalculator {

    public BMI calculate(UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Medidas corporais não podem ser nulo");
        return calculate(metrics.getHeight(), metrics.getWeight());
    }

    public BMI calculate(BodyHeightCm height, BodyWeightKg weight) {
        Objects.requireNonNull(height, "Altura não pode ser nulo");
        Objects.requireNonNull(weight, "Peso não pode ser nulo");

        double result = weight.value() / (height.inMeters() * height.inMeters());

        return new BMI(result);
    }
}
