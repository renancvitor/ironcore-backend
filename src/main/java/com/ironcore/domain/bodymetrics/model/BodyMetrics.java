package com.ironcore.domain.bodymetrics.model;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BodyMetrics {

    private final BodyMetricsId id;
    private final PersonId personId;
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

    private BodyMetrics(BodyMetricsId id, PersonId personId, LocalDateTime measuredAt, BodyWeightKg weight,
                       BodyHeightCm height, BodyCircumferences circumferences, BMI bmi,
                       BodyFatPercentage bodyFatPercentage, FatMassKg fatMassKg,
                       LeanMassKg leanMassKg, LocalDateTime updatedAt, String notes) {
        this.id = id;
        this.personId = requireNonNull(personId, "Pessoa não pode ser nula.");
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
            PersonId personId,
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
                personId,
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
            PersonId personId,
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
                personId,
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
