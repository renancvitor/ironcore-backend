package com.ironcore.domain.userbodymetrics.model;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.valueobject.*;

import java.time.LocalDateTime;

public class UserBodyMetrics {

    private UserBodyMetricsId id;
    private User user;
    private LocalDateTime measuredAt;
    private BodyWeightKg weight;
    private BodyHeightCm height;
    private BodyCircumferences circumferences;
    private BMI bmi;
    private BodyFatPercentage bodyFatPercentage;
    private FatMassKg fatMassKg;
    private LeanMassKg leanMassKg;
    private String notes;

    public UserBodyMetrics() {
    }

    public UserBodyMetrics(UserBodyMetricsId id, User user, LocalDateTime measuredAt, BodyWeightKg weight,
                           BodyHeightCm height, BodyCircumferences circumferences, BMI bmi,
                           BodyFatPercentage bodyFatPercentage, FatMassKg fatMassKg,
                           LeanMassKg leanMassKg, String notes) {
        this.id = id;
        this.user = requireNonNull(user, "Usuário não pode ser nulo");
        this.measuredAt = requireNonNull(measuredAt, "Data de medição não pode ser nulo");
        this.weight = requireNonNull(weight, "Peso não pode ser nulo");
        this.height = requireNonNull(height, "Altura não pode ser nulo");
        this.circumferences = circumferences;
        this.bmi = bmi;
        this.bodyFatPercentage = bodyFatPercentage;
        this.fatMassKg = fatMassKg;
        this.leanMassKg = leanMassKg;
        this.notes = notes;
    }

    public UserBodyMetricsId getId() {
        return id;
    }

    public void setId(UserBodyMetricsId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = requireNonNull(user, "Usuário não pode ser nulo");
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = requireNonNull(measuredAt, "Data de medição não pode ser nulo");
    }

    public BodyWeightKg getWeight() {
        return weight;
    }

    public void setWeight(BodyWeightKg weight) {
        this.weight = requireNonNull(weight, "Peso não pode ser nulo");
    }

    public BodyHeightCm getHeight() {
        return height;
    }

    public void setHeight(BodyHeightCm height) {
        this.height = requireNonNull(height, "Altura não pode ser nulo");
    }

    public BodyCircumferences getCircumferences() {
        return circumferences;
    }

    public void setCircumferences(BodyCircumferences circumferences) {
        this.circumferences = circumferences;
    }

    public BodyFatPercentage getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(BodyFatPercentage bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public BMI getBmi() {
        return bmi;
    }

    public void setBmi(BMI bmi) {
        this.bmi = bmi;
    }

    public FatMassKg getFatMassKg() {
        return fatMassKg;
    }

    public void setFatMassKg(FatMassKg fatMassKg) {
        this.fatMassKg = fatMassKg;
    }

    public LeanMassKg getLeanMassKg() {
        return leanMassKg;
    }

    public void setLeanMassKg(LeanMassKg leanMassKg) {
        this.leanMassKg = leanMassKg;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
