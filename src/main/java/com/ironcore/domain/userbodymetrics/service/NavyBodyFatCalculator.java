package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.user.model.Sex;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;

import java.util.Objects;

public class NavyBodyFatCalculator {

    private static final double CENTIMETERS_PER_INCH = 2.54;

    public BodyFatPercentage calculate(UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Body metrics are required");
        Objects.requireNonNull(metrics.getUser(), "User is required");
        return calculate(metrics.getUser().getSex(), metrics);
    }

    public BodyFatPercentage calculate(Sex sex, UserBodyMetrics metrics) {
        Objects.requireNonNull(metrics, "Body metrics are required");
        return calculate(sex, metrics.getHeight(), metrics.getCircumferences());
    }

    public BodyFatPercentage calculate(Sex sex, BodyHeightCm height, BodyCircumferences circumferences) {
        Objects.requireNonNull(sex, "Sex is required");
        Objects.requireNonNull(height, "Body height is required");
        Objects.requireNonNull(circumferences, "Body circumferences are required");

        double heightIn = toInches(height.value());
        double waistIn = toInches(required(circumferences.waist(), "Waist circumference is required").value());
        double neckIn = toInches(required(circumferences.neck(), "Neck circumference is required").value());

        double result = switch (sex) {
            case MALE -> calculateForMale(heightIn, waistIn, neckIn);
            case FEMALE -> calculateForFemale(heightIn, waistIn, neckIn,
                    toInches(required(circumferences.hip(), "Hip circumference is required").value()));
        };

        return new BodyFatPercentage(result);
    }

    private double calculateForMale(double heightIn, double waistIn, double neckIn) {
        double circumferenceDifference = waistIn - neckIn;
        if (circumferenceDifference <= 0) {
            throw new IllegalArgumentException("Waist circumference must be greater than neck circumference");
        }

        return 86.010 * Math.log10(circumferenceDifference)
                - 70.041 * Math.log10(heightIn)
                + 36.76;
    }

    private double calculateForFemale(double heightIn, double waistIn, double neckIn, double hipIn) {
        double circumferenceValue = waistIn + hipIn - neckIn;
        if (circumferenceValue <= 0) {
            throw new IllegalArgumentException("Waist plus hip circumference must be greater than neck circumference");
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
