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
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivityValidation.*;

@Getter
public final class WorkoutSessionActivity {
    private final WorkoutSessionActivityId id;
    private final WorkoutActivityId workoutActivityId;
    private final ExerciseId exerciseId;
    private final WorkoutSessionActivitySnapshot snapshot;
    private final Integer actualDurationMinutes;
    private final BigDecimal actualDistanceKm;
    private final String notes;
    private final Instant createdAt;
    private final Instant updatedAt;
    @Getter(lombok.AccessLevel.NONE)
    private final List<WorkoutSessionSet> sets;

    private WorkoutSessionActivity(WorkoutSessionActivityId id, WorkoutActivityId origin, ExerciseId exerciseId,
                                  WorkoutSessionActivitySnapshot snapshot, Integer duration, BigDecimal distance,
                                  String notes, Instant createdAt, Instant updatedAt, List<WorkoutSessionSet> sets) {
        this.id = id;
        this.workoutActivityId = origin;
        this.exerciseId = exerciseId;
        this.snapshot = required(snapshot, "Snapshot da atividade");
        positive(duration, "Duração executada");
        decimal(distance, "Distância executada");
        this.actualDurationMinutes = duration;
        this.actualDistanceKm = distance;
        this.notes = notes;
        this.createdAt = required(createdAt, "Data de criação");
        if (updatedAt != null) timestamp(updatedAt, createdAt);
        this.updatedAt = updatedAt;
        this.sets = new ArrayList<>(required(sets, "Séries"));
        if (this.sets.stream().anyMatch(Objects::isNull)
                || this.sets.stream().map(WorkoutSessionSet::getSetNumber).distinct().count() != this.sets.size())
            throw new InvalidWorkoutSessionActivityException("Números de série devem ser únicos.");
        validateModes(this.sets);
    }

    public static WorkoutSessionActivity capture(WorkoutActivity activity, Exercise exercise, ActivityType type, Instant now) {
        required(activity, "Atividade de origem");
        return new WorkoutSessionActivity(null, required(activity.getId(), "Id da atividade de origem"),
                required(exercise, "Exercício").getId(),
                WorkoutSessionActivitySnapshot.capture(activity, exercise, type),
                null, null, null, now, null, List.of());
    }

    public static WorkoutSessionActivity restore(WorkoutSessionActivityId id, WorkoutActivityId origin, ExerciseId exerciseId,
                                                 WorkoutSessionActivitySnapshot snapshot, Integer duration, BigDecimal distance,
                                                 String notes, Instant createdAt, Instant updatedAt, List<WorkoutSessionSet> sets) {
        return new WorkoutSessionActivity(required(id, "Id da atividade"), origin, exerciseId, snapshot,
                duration, distance, notes, createdAt, updatedAt, sets);
    }

    public List<WorkoutSessionSet> getSets() { return List.copyOf(sets); }

    public WorkoutSessionActivity updateExecution(Integer duration, BigDecimal distance, String notes, Instant now) {
        positive(duration, "Duração executada");
        decimal(distance, "Distância executada");
        timestamp(now, updatedAt != null ? updatedAt : createdAt);

        return new WorkoutSessionActivity(id, workoutActivityId, exerciseId, snapshot, duration, distance,
                notes, createdAt, now, sets);
    }

    public WorkoutSessionActivity recordSet(Integer number, BigDecimal load, ActualLoadMode mode,
                                            Integer reps, String notes, Instant now) {
        timestamp(now, updatedAt != null ? updatedAt : createdAt);
        WorkoutSessionSet previous = sets.stream().filter(s -> s.getSetNumber().equals(number)).findFirst().orElse(null);
        WorkoutSessionSet next = previous == null ? WorkoutSessionSet.register(number, load, mode, reps, notes, now)
                : previous.update(load, mode, reps, notes, now);
        List<WorkoutSessionSet> candidate = new ArrayList<>(sets);
        candidate.remove(previous);
        candidate.add(next);
        validateModes(candidate);
        candidate.sort(Comparator.comparing(WorkoutSessionSet::getSetNumber));

        return withSets(candidate, now);
    }

    public WorkoutSessionActivity removeSet(Integer number, Instant now) {
        timestamp(now, updatedAt != null ? updatedAt : createdAt);
        List<WorkoutSessionSet> remaining = sets.stream()
                .filter(s -> !s.getSetNumber().equals(number)).toList();

        return withSets(remaining, now);
    }

    public WorkoutSessionActivity changeLoadMode(ActualLoadMode mode, Instant now) {
        required(mode, "Modo da carga");
        timestamp(now, updatedAt != null ? updatedAt : createdAt);
        List<WorkoutSessionSet> changed = sets.stream().map(s -> s.changeMode(mode, now)).toList();

        return withSets(changed, now);
    }

    private WorkoutSessionActivity withSets(List<WorkoutSessionSet> sets, Instant now) {
        return new WorkoutSessionActivity(id, workoutActivityId, exerciseId, snapshot,
                actualDurationMinutes, actualDistanceKm, notes, createdAt, now, sets);
    }

    private static void validateModes(List<WorkoutSessionSet> sets) {
        if (sets.stream().map(WorkoutSessionSet::getActualLoadMode).filter(Objects::nonNull).distinct().count() > 1)
            throw new InvalidWorkoutSessionActivityException("As séries da atividade devem utilizar o mesmo modo de carga.");
    }
}
