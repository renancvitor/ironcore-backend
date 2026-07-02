package com.ironcore.domain.bodymetrics.service;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;

public class NavyBodyFatCalculator {

    private static final double CENTIMETERS_PER_INCH = 2.54;

    public BodyFatPercentage calculate(SexType sex, BodyMetrics metrics) {
        requireNonNull(metrics, "Medidas corporais são obrigatórias.");
        return calculate(sex, metrics.getHeight(), metrics.getCircumferences());
    }

    public BodyFatPercentage calculate(SexType sex, BodyHeightCm height, BodyCircumferences circumferences) {
        requireNonNull(sex, "Sexo é obrigatório.");
        requireNonNull(height, "Altura é obrigatória.");
        requireNonNull(circumferences, "Circunferências corporais são obrigatórias.");

        BodyCircumferenceCm waist = required(circumferences.waist(), "Circunferência da cintura é obrigatória.");
        BodyCircumferenceCm neck = required(circumferences.neck(), "Circunferência do pescoço é obrigatória.");

        double heightIn = toInches(height.value());
        double waistIn = toInches(waist.value());
        double neckIn = toInches(neck.value());

        double result = switch (sex) {
            case MALE -> calculateForMale(heightIn, waistIn, neckIn);
            case FEMALE -> calculateForFemale(heightIn, waistIn, neckIn, requiredHipIn(circumferences));
        };

        return new BodyFatPercentage(result);
    }

    private double calculateForMale(double heightIn, double waistIn, double neckIn) {
        double circumferenceDifference = waistIn - neckIn;
        if (circumferenceDifference <= 0) {
            throw new InvalidBodyMetricException(
                    "Circunferência da cintura deve ser maior do que a circunferência do pescoço."
            );
        }

        return 86.010 * Math.log10(circumferenceDifference)
                - 70.041 * Math.log10(heightIn)
                + 36.76;
    }

    private double calculateForFemale(double heightIn, double waistIn, double neckIn, double hipIn) {
        double circumferenceValue = waistIn + hipIn - neckIn;
        if (circumferenceValue <= 0) {
            throw new InvalidBodyMetricException(
                    "Circunferência da cintura somada à do quadril deve ser maior que a circunferência do pescoço."
            );
        }

        return 163.205 * Math.log10(circumferenceValue)
                - 97.684 * Math.log10(heightIn)
                - 78.387;
    }

    private double toInches(double centimeters) {
        return centimeters / CENTIMETERS_PER_INCH;
    }

    private double requiredHipIn(BodyCircumferences circumferences) {
        BodyCircumferenceCm hip = required(circumferences.hip(), "Circunferência do quadril é obrigatória.");
        return toInches(hip.value());
    }

    private BodyCircumferenceCm required(BodyCircumferenceCm value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
