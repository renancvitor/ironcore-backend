package com.ironcore.domain.workoutsession.workoutsessionactivity.model;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
public class WorkoutSessionActivity {

    private final WorkoutSessionActivityId id;
    private final WorkoutActivityId workoutActivityId;
    private final ExerciseId exerciseId;
    private final WorkoutSessionActivitySnapshot snapshot;
    private Integer actualDurationMinutes;
    private BigDecimal actualDistanceKm;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    @Getter(AccessLevel.NONE)
    private final List<WorkoutSessionSet> sets;

    private WorkoutSessionActivity(
            WorkoutSessionActivityId id,
            WorkoutActivityId origin,
            ExerciseId exerciseId,
            WorkoutSessionActivitySnapshot snapshot,
            Integer duration,
            BigDecimal distance,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            List<WorkoutSessionSet> sets
    ) {
        this.id = id;
        this.workoutActivityId = origin;
        this.exerciseId = exerciseId;
        this.snapshot = requireNonNull(snapshot, "Snapshot da atividade");
        requirePositiveIfPresent(duration, "Duração executada");
        validateDecimal(distance, "Distância executada");
        this.actualDurationMinutes = duration;
        this.actualDistanceKm = distance;
        this.notes = notes;
        this.createdAt = requireNonNull(createdAt, "Data de criação");
        if (updatedAt != null) {
            validateUpdateTime(updatedAt, createdAt);
        }
        this.updatedAt = updatedAt;
        this.sets = new ArrayList<>(requireNonNull(sets, "Séries"));
        if (this.sets.stream().anyMatch(Objects::isNull)
                || this.sets.stream().map(WorkoutSessionSet::getSetNumber).distinct().count() != this.sets.size()) {
            throw new InvalidWorkoutSessionActivityException("Números de série devem ser únicos.");
        }
        validateModes(this.sets);
    }

    public static WorkoutSessionActivity register(
            WorkoutActivity activity,
            Exercise exercise,
            ActivityType type,
            Instant now
    ) {
        requireNonNull(activity, "Atividade de origem");
        return new WorkoutSessionActivity(null, requireNonNull(activity.getId(), "Id da atividade de origem"),
                requireNonNull(exercise, "Exercício").getId(),
                WorkoutSessionActivitySnapshot.capture(activity, exercise, type),
                null, null, null, now, null, List.of());
    }

    public static WorkoutSessionActivity restore(
            WorkoutSessionActivityId id,
            WorkoutActivityId origin,
            ExerciseId exerciseId,
            WorkoutSessionActivitySnapshot snapshot,
            Integer duration,
            BigDecimal distance,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            List<WorkoutSessionSet> sets
    ) {
        return new WorkoutSessionActivity(requireNonNull(id, "Id da atividade"), origin, exerciseId, snapshot,
                duration, distance, notes, createdAt, updatedAt, sets);
    }

    public List<WorkoutSessionSet> getSets() {
        return List.copyOf(sets);
    }

    public void updateExecution(
            Integer duration,
            BigDecimal distance,
            String notes,
            Instant updatedAt
    ) {
        requirePositiveIfPresent(duration, "Duração executada");
        validateDecimal(distance, "Distância executada");
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);

        this.actualDurationMinutes = duration;
        this.actualDistanceKm = distance;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public void addSet(
            Integer number,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant updatedAt
    ) {
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);
        if (sets.stream().anyMatch(set -> set.getSetNumber().equals(number))) {
            throw new InvalidWorkoutSessionActivityException("Número de série já registrado.");
        }
        validateLoadMode(mode, null);
        WorkoutSessionSet set = WorkoutSessionSet.register(number, load, mode, reps, notes, updatedAt);
        sets.add(set);
        sets.sort(Comparator.comparing(WorkoutSessionSet::getSetNumber));
        this.updatedAt = updatedAt;
    }

    public void updateSet(
            WorkoutSessionSetId setId,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant updatedAt
    ) {
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);
        WorkoutSessionSet set = findSet(setId);
        validateLoadMode(mode, setId);
        set.updateSet(load, mode, reps, notes, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void removeSet(WorkoutSessionSetId setId, Instant updatedAt) {
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);
        WorkoutSessionSet set = findSet(setId);
        sets.remove(set);
        this.updatedAt = updatedAt;
    }

    public void changeLoadMode(ActualLoadMode mode, Instant updatedAt) {
        requireNonNull(mode, "Modo da carga");
        validateUpdateTime(updatedAt, this.updatedAt != null ? this.updatedAt : createdAt);
        for (WorkoutSessionSet set : sets) {
            validateUpdateTime(updatedAt, set.getUpdatedAt() != null ? set.getUpdatedAt() : set.getCreatedAt());
        }
        for (WorkoutSessionSet set : sets) {
            set.changeLoadMode(mode, updatedAt);
        }
        this.updatedAt = updatedAt;
    }

    public void validateSets() {
        validateModes(sets);
    }

    private WorkoutSessionSet findSet(WorkoutSessionSetId setId) {
        requireNonNull(setId, "Id da série");
        return sets.stream()
                .filter(set -> setId.equals(set.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidWorkoutSessionActivityException("Série não pertence à atividade."));
    }

    private void validateLoadMode(ActualLoadMode mode, WorkoutSessionSetId excludedId) {
        if (mode != null && sets.stream()
                .filter(set -> excludedId == null || !excludedId.equals(set.getId()))
                .map(WorkoutSessionSet::getActualLoadMode)
                .filter(Objects::nonNull)
                .anyMatch(existingMode -> existingMode != mode)) {
            throw new InvalidWorkoutSessionActivityException("As séries da atividade devem utilizar o mesmo modo de carga.");
        }
    }

    private static void validateModes(List<WorkoutSessionSet> sets) {
        if (sets.stream().map(WorkoutSessionSet::getActualLoadMode).filter(Objects::nonNull).distinct().count() > 1) {
            throw new InvalidWorkoutSessionActivityException("As séries da atividade devem utilizar o mesmo modo de carga.");
        }
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidWorkoutSessionActivityException(field + " é obrigatório.");
        }

        return value;
    }

    private void requirePositiveIfPresent(Integer value, String field) {
        if (value != null && value <= 0) {
            throw new InvalidWorkoutSessionActivityException(field + " deve ser positivo.");
        }
    }

    private void validateDecimal(BigDecimal value, String field) {
        if (value != null && (value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                || value.compareTo(new BigDecimal("999999.99")) > 0)) {
            throw new InvalidWorkoutSessionActivityException(field + " deve estar entre 0 e 999999.99, com até duas casas decimais.");
        }
    }

    private void validateUpdateTime(Instant value, Instant createdAt) {
        requireNonNull(value, "Data de atualização");

        if (value.isBefore(createdAt)) {
            throw new InvalidWorkoutSessionActivityException("Data de atualização anterior à criação.");
        }
    }
}
