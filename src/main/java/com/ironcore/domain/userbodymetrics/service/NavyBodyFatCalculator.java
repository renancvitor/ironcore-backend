package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.user.model.SexType;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;

import java.util.Objects;

public class NavyBodyFatCalculator {

    private static final double CENTIMETERS_PER_INCH = 2.54;

    public BodyFatPercentage calculate(UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Medidas corporais não podem ser nulo");
        Objects.requireNonNull(metrics.getUser(), "Usuário não pode ser nulo");
        return calculate(metrics.getUser().getSex().type(), metrics);
    }

    public BodyFatPercentage calculate(SexType sex, UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Medidas corporais não podem ser nulo");
        return calculate(sex, metrics.getHeight(), metrics.getCircumferences());
    }

    public BodyFatPercentage calculate(SexType sex, BodyHeightCm height, BodyCircumferences circumferences) {
        Objects.requireNonNull(sex, "Sexo não pode ser nulo");
        Objects.requireNonNull(height, "Peso não pode ser nulo");
        Objects.requireNonNull(circumferences, "Circunferências corporais não pode ser nulo");

        double heightIn = toInches(height.value());
        double waistIn = toInches(required(circumferences.waist(), "Circunferência da cintura não pode ser nulo").value());
        double neckIn = toInches(required(circumferences.neck(), "Circunferência do pescoço não pode ser nulo").value());

        double result = switch (sex) {
            case MALE -> calculateForMale(heightIn, waistIn, neckIn);
            case FEMALE -> calculateForFemale(heightIn, waistIn, neckIn,
                    toInches(required(circumferences.hip(), "Circunferência do quadril não pode ser nulo").value()));
        };

        return new BodyFatPercentage(result);
    }

    private double calculateForMale(double heightIn, double waistIn, double neckIn) {
        double circumferenceDifference = waistIn - neckIn;
        if (circumferenceDifference <= 0) {
            throw new IllegalArgumentException("Circunferência da cintura deve ser maior do que a circunferência do pescoço");
        }

        return 86.010 * Math.log10(circumferenceDifference)
                - 70.041 * Math.log10(heightIn)
                + 36.76;
    }

    private double calculateForFemale(double heightIn, double waistIn, double neckIn, double hipIn) {
        double circumferenceValue = waistIn + hipIn - neckIn;
        if (circumferenceValue <= 0) {
            throw new IllegalArgumentException("Circunferência da cintura somada à do quadril deve ser maior que a circunferência do pescoço");
        }

        return 163.205 * Math.log10(circumferenceValue)
                - 97.684 * Math.log10(heightIn)
                - 78.387;
    }

    private double toInches(double centimeters) {
        return centimeters / CENTIMETERS_PER_INCH;
    }

    private BodyCircumferenceCm required(BodyCircumferenceCm value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
