package com.ironcore.domain.userbodymetrics.model;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserBodyMetrics {

    private final UserBodyMetricsId id;
    private final UserId userId;
    private LocalDateTime measuredAt;
    private BodyWeightKg weight;
    private BodyHeightCm height;
    private BodyCircumferences circumferences;
    private BMI bmi;
    private BodyFatPercentage bodyFatPercentage;
    private FatMassKg fatMassKg;
    private LeanMassKg leanMassKg;
    private LocalDateTime updatedAt;
    private String notes;

    public UserBodyMetrics(UserBodyMetricsId id, UserId userId, LocalDateTime measuredAt, BodyWeightKg weight,
                           BodyHeightCm height, BodyCircumferences circumferences, BMI bmi,
                           BodyFatPercentage bodyFatPercentage, FatMassKg fatMassKg,
                           LeanMassKg leanMassKg, LocalDateTime updatedAt, String notes) {
        this.id = id;
        this.userId = requireNonNull(userId, "Usuário não pode ser nulo.");
        this.measuredAt = requireNonNull(measuredAt, "Data de medição é obrigatória.");
        this.weight = requireNonNull(weight, "Peso é obrigatório.");
        this.height = requireNonNull(height, "Altura é obrigatória.");
        this.circumferences = circumferences;
        this.bmi = bmi;
        this.bodyFatPercentage = bodyFatPercentage;
        this.fatMassKg = fatMassKg;
        this.leanMassKg = leanMassKg;
        this.updatedAt = updatedAt;
        this.notes = notes;
    }

    public static UserBodyMetrics register(
            UserId userId,
            LocalDateTime measuredAt,
            BodyWeightKg weight,
            BodyHeightCm height,
            BodyCircumferences circumferences,
            BMI bmi,
            BodyFatPercentage bodyFatPercentage,
            FatMassKg fatMassKg,
            LeanMassKg leanMassKg,
            String notes
    ) {
        return new UserBodyMetrics(
                null,
                userId,
                measuredAt,
                weight,
                height,
                circumferences,
                bmi,
                bodyFatPercentage,
                fatMassKg,
                leanMassKg,
                null,
                notes
        );
    }

    public static UserBodyMetrics restore(
            UserBodyMetricsId id,
            UserId userId,
            LocalDateTime measuredAt,
            BodyWeightKg weight,
            BodyHeightCm height,
            BodyCircumferences circumferences,
            BMI bmi,
            BodyFatPercentage bodyFatPercentage,
            FatMassKg fatMassKg,
            LeanMassKg leanMassKg,
            LocalDateTime updatedAt,
            String notes
    ) {
        return new UserBodyMetrics(
                id,
                userId,
                measuredAt,
                weight,
                height,
                circumferences,
                bmi,
                bodyFatPercentage,
                fatMassKg,
                leanMassKg,
                updatedAt,
                notes
        );
    }

    public void updateMeasurements(
            LocalDateTime measuredAt,
            BodyWeightKg weight,
            BodyHeightCm height,
            BodyCircumferences circumferences,
            BMI bmi,
            BodyFatPercentage bodyFatPercentage,
            FatMassKg fatMassKg,
            LeanMassKg leanMassKg,
            String notes,
            LocalDateTime updatedAt
    ) {
        this.measuredAt = requireNonNull(measuredAt, "Data de medição é obrigatória.");
        this.weight = requireNonNull(weight, "Peso é obrigatório.");
        this.height = requireNonNull(height, "Altura é obrigatória.");
        this.circumferences = circumferences;
        this.bmi = bmi;
        this.bodyFatPercentage = bodyFatPercentage;
        this.fatMassKg = fatMassKg;
        this.leanMassKg = leanMassKg;
        this.notes = notes;
        markUpdatedAt(updatedAt);
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidBodyMetricException(message);
        }

        return value;
    }
}
