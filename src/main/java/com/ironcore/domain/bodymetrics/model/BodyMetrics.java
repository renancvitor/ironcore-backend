package com.ironcore.domain.bodymetrics.model;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BodyMetrics {

    private final BodyMetricsId id;
    private final UserId userId;
    private final LocalDateTime measuredAt;
    private BodyWeightKg weight;
    private BodyHeightCm height;
    private BodyCircumferences circumferences;
    private BMI bmi;
    private BodyFatPercentage bodyFatPercentage;
    private FatMassKg fatMassKg;
    private LeanMassKg leanMassKg;
    private LocalDateTime updatedAt;
    private String notes;

    public BodyMetrics(BodyMetricsId id, UserId userId, LocalDateTime measuredAt, BodyWeightKg weight,
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

    public static BodyMetrics register(
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
        return new BodyMetrics(
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

    public static BodyMetrics restore(
            BodyMetricsId id,
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
        return new BodyMetrics(
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
