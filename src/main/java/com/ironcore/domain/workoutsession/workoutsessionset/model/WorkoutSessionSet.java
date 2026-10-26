package com.ironcore.domain.workoutsession.workoutsessionset.model;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

import static com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSetValidation.*;

@Getter
public final class WorkoutSessionSet {
    private final WorkoutSessionSetId id;
    private final Integer setNumber;
    private final BigDecimal actualLoadKg;
    private final ActualLoadMode actualLoadMode;
    private final Integer actualReps;
    private final String notes;
    private final Instant createdAt;
    private final Instant updatedAt;

    private WorkoutSessionSet(WorkoutSessionSetId id, Integer setNumber, BigDecimal actualLoadKg,
                              ActualLoadMode actualLoadMode, Integer actualReps, String notes,
                              Instant createdAt, Instant updatedAt) {
        required(setNumber, "Número da série");
        positive(setNumber, "Número da série");
        decimal(actualLoadKg, "Carga executada");
        positive(actualReps, "Repetições executadas");
        if ((actualLoadKg == null) != (actualReps == null))
            throw new InvalidWorkoutSessionSetException("Carga e repetições devem ser informadas juntas.");
        if (actualLoadKg != null && actualLoadMode == null)
            throw new InvalidWorkoutSessionSetException("Modo da carga é obrigatório quando houver carga.");
        if (actualLoadKg == null && actualLoadMode != null)
            throw new InvalidWorkoutSessionSetException("Modo da carga exige carga informada.");
        if (actualLoadKg == null && (notes == null || notes.isBlank()))
            throw new InvalidWorkoutSessionSetException("Série vazia não deve ser persistida.");
        this.id = id;
        this.setNumber = setNumber;
        this.actualLoadKg = actualLoadKg;
        this.actualLoadMode = actualLoadMode;
        this.actualReps = actualReps;
        this.notes = notes;
        this.createdAt = required(createdAt, "Data de criação");
        if (updatedAt != null) timestamp(updatedAt, createdAt);
        this.updatedAt = updatedAt;
    }

    public static WorkoutSessionSet register(Integer number, BigDecimal load, ActualLoadMode mode,
                                            Integer reps, String notes, Instant now) {
        return new WorkoutSessionSet(null, number, load, mode, reps, notes, now, null);
    }

    public static WorkoutSessionSet restore(WorkoutSessionSetId id, Integer number, BigDecimal load,
                                           ActualLoadMode mode, Integer reps, String notes,
                                           Instant createdAt, Instant updatedAt) {
        return new WorkoutSessionSet(required(id, "Id da série"), number, load, mode, reps, notes, createdAt, updatedAt);
    }

    public WorkoutSessionSet update(BigDecimal load, ActualLoadMode mode, Integer reps, String notes, Instant now) {
        timestamp(now, updatedAt != null ? updatedAt : createdAt);

        return new WorkoutSessionSet(id, setNumber, load, mode, reps, notes, createdAt, now);
    }

    public WorkoutSessionSet changeMode(ActualLoadMode mode, Instant now) {
        return update(actualLoadKg, actualLoadKg == null ? null : mode, actualReps, notes, now);
    }
}
