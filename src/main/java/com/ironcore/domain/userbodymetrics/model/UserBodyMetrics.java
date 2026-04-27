package com.ironcore.domain.userbodymetrics.model;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserBodyMetrics {

    private UserBodyMetricsId id;
    private User user;
    private LocalDateTime measuredAt;
    private BodyWeightKg weight;
    private BodyHeightCm height;
    private BodyFatPercentage bodyFatPercentage;
    private BodyCircumferences circumferences;
    private String notes;

    public UserBodyMetrics() {
    }

    public UserBodyMetrics(UserBodyMetricsId id, User user, LocalDateTime measuredAt, BodyWeightKg weight,
                           BodyHeightCm height, BodyFatPercentage bodyFatPercentage,
                           BodyCircumferences circumferences, String notes) {
        this.id = id;
        this.user = Objects.requireNonNull(user, "User is required");
        this.measuredAt = Objects.requireNonNull(measuredAt, "Measurement date is required");
        this.weight = Objects.requireNonNull(weight, "Body weight is required");
        this.height = Objects.requireNonNull(height, "Body height is required");
        this.bodyFatPercentage = bodyFatPercentage;
        this.circumferences = circumferences;
        this.notes = notes;
    }

    public double calculateBmi() {
        double heightInMeters = height.inMeters();
        return weight.value() / (heightInMeters * heightInMeters);
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
        this.user = Objects.requireNonNull(user, "User is required");
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = Objects.requireNonNull(measuredAt, "Measurement date is required");
    }

    public BodyWeightKg getWeight() {
        return weight;
    }

    public void setWeight(BodyWeightKg weight) {
        this.weight = Objects.requireNonNull(weight, "Body weight is required");
    }

    public BodyHeightCm getHeight() {
        return height;
    }

    public void setHeight(BodyHeightCm height) {
        this.height = Objects.requireNonNull(height, "Body height is required");
    }

    public BodyFatPercentage getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(BodyFatPercentage bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public BodyCircumferences getCircumferences() {
        return circumferences;
    }

    public void setCircumferences(BodyCircumferences circumferences) {
        this.circumferences = circumferences;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
