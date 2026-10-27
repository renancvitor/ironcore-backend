package com.ironcore.domain.workoutsession.workoutsessionset.model;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class WorkoutSessionSet {

    private final WorkoutSessionSetId id;
    private final Integer setNumber;
    private BigDecimal actualLoadKg;
    private ActualLoadMode actualLoadMode;
    private Integer actualReps;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    private WorkoutSessionSet(
            WorkoutSessionSetId id,
            Integer setNumber,
            BigDecimal actualLoadKg,
            ActualLoadMode actualLoadMode,
            Integer actualReps,
            String notes,
            Instant createdAt,
            Instant updatedAt
    ) {
        requireNonNull(setNumber, "Número da série");
        requirePositiveIfPresent(setNumber, "Número da série");
        validateExecution(actualLoadKg, actualLoadMode, actualReps, notes);
        this.id = id;
        this.setNumber = setNumber;
        this.actualLoadKg = actualLoadKg;
        this.actualLoadMode = actualLoadMode;
        this.actualReps = actualReps;
        this.notes = notes;
        this.createdAt = requireNonNull(createdAt, "Data de criação");
        if (updatedAt != null) {
            validateUpdateTime(updatedAt, createdAt);
        }
        this.updatedAt = updatedAt;
    }

    public static WorkoutSessionSet register(
            Integer number,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant now
    ) {
        return new WorkoutSessionSet(null, number, load, mode, reps, notes, now, null);
    }

    public static WorkoutSessionSet restore(
            WorkoutSessionSetId id,
            Integer number,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new WorkoutSessionSet(requireNonNull(id, "Id da série"), number, load, mode, reps, notes, createdAt, updatedAt);
    }

    public void updateSet(
            BigDecimal actualLoadKg,
            ActualLoadMode actualLoadMode,
            Integer actualReps,
            String notes,
            Instant updatedAt
    ) {
        validateExecution(actualLoadKg, actualLoadMode, actualReps, notes);
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);

        this.actualLoadKg = actualLoadKg;
        this.actualLoadMode = actualLoadMode;
        this.actualReps = actualReps;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public void changeLoadMode(ActualLoadMode mode, Instant updatedAt) {
        updateSet(actualLoadKg, actualLoadKg == null ? null : mode, actualReps, notes, updatedAt);
    }

    private void validateExecution(
            BigDecimal actualLoadKg,
            ActualLoadMode actualLoadMode,
            Integer actualReps,
            String notes
    ) {
        validateDecimal(actualLoadKg, "Carga executada");
        requirePositiveIfPresent(actualReps, "Repetições executadas");
        if ((actualLoadKg == null) != (actualReps == null)) {
            throw new InvalidWorkoutSessionSetException("Carga e repetições devem ser informadas juntas.");
        }
        if (actualLoadKg != null && actualLoadMode == null) {
            throw new InvalidWorkoutSessionSetException("Modo da carga é obrigatório quando houver carga.");
        }
        if (actualLoadKg == null && actualLoadMode != null) {
            throw new InvalidWorkoutSessionSetException("Modo da carga exige carga informada.");
        }
        if (actualLoadKg == null && (notes == null || notes.isBlank())) {
            throw new InvalidWorkoutSessionSetException("Série vazia não deve ser persistida.");
        }
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidWorkoutSessionSetException(field + " é obrigatório.");
        }

        return value;
    }

    private void requirePositiveIfPresent(Integer value, String field) {
        if (value != null && value <= 0) {
            throw new InvalidWorkoutSessionSetException(field + " deve ser positivo.");
        }
    }

    private void validateDecimal(BigDecimal value, String field) {
        if (value != null && (value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                || value.compareTo(new BigDecimal("999999.99")) > 0)) {
            throw new InvalidWorkoutSessionSetException(field + " deve estar entre 0 e 999999.99, com até duas casas decimais.");
        }
    }

    private void validateUpdateTime(Instant value, Instant createdAt) {
        requireNonNull(value, "Data de atualização");

        if (value.isBefore(createdAt)) {
            throw new InvalidWorkoutSessionSetException("Data de atualização anterior à criação.");
        }
    }
}
