package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.FatMassKg;
import com.ironcore.domain.userbodymetrics.valueobject.LeanMassKg;

import java.util.Objects;

public class LeanMassCalculator {

    public LeanMassKg calculate(UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Medidas corporais não podem ser nulo");
        return calculate(metrics.getWeight(), metrics.getFatMassKg());
    }

    public LeanMassKg calculate(BodyWeightKg weight, FatMassKg fatMass) {
        Objects.requireNonNull(weight, "Peso não pode ser nulo");
        Objects.requireNonNull(fatMass, "Massa gorda não pode ser nulo");

        double result = weight.value() - fatMass.value();

        return new LeanMassKg(result);
    }
}
