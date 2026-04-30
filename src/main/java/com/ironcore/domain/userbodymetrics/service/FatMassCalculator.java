package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.FatMassKg;

import java.util.Objects;

public class FatMassCalculator {

    public FatMassKg calculate(UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Medidas corporais não podem ser nulo");
        return calculate(metrics.getWeight(), metrics.getBodyFatPercentage());
    }

    public FatMassKg calculate(BodyWeightKg weight, BodyFatPercentage bodyFatPercentage) {
        Objects.requireNonNull(weight, "Peso não pode ser nulo");
        Objects.requireNonNull(bodyFatPercentage, "Percentual de gordura não pode ser nulo");

        double result = (weight.value() * bodyFatPercentage.value()) / 100;

        return new FatMassKg(result);
    }
}
