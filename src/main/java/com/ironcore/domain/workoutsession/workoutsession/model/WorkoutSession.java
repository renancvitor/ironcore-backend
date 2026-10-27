package com.ironcore.domain.workoutsession.workoutsession.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
public class WorkoutSession {

    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final WorkoutSessionId id;
    private final PersonId personId;
    private final WorkoutCycleId workoutCycleId;
    private final WorkoutDayId workoutDayId;
    private final WorkoutSessionSnapshot snapshot;
    private final LocalDate performedOn;
    private final Instant startedAt;
    private Instant finishedAt;
    private WorkoutSessionStatus status;
    private WorkoutSessionCloseReason closeReason;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Long version;

    @Getter(AccessLevel.NONE)
    private final List<WorkoutSessionActivity> activities;

    private WorkoutSession(
            WorkoutSessionId id,
            PersonId personId,
            WorkoutCycleId cycleId,
            WorkoutDayId dayId,
            WorkoutSessionSnapshot snapshot,
            LocalDate performedOn,
            Instant startedAt,
            Instant finishedAt,
            WorkoutSessionStatus status,
            WorkoutSessionCloseReason reason,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            Long version,
            List<WorkoutSessionActivity> activities
    ) {
        this.id = id;
        this.personId = requireNonNull(personId, "Pessoa");
        this.workoutCycleId = cycleId;
        this.workoutDayId = dayId;
        this.snapshot = requireNonNull(snapshot, "Snapshot da sessão");
        this.startedAt = requireNonNull(startedAt, "Início");
        this.performedOn = requireNonNull(performedOn, "Data local de início");
        if (!performedOn.equals(startedAt.atZone(ZONE).toLocalDate())) {
            throw new InvalidWorkoutSessionException("Data de referência deve corresponder ao início no fuso da sessão.");
        }
        this.status = requireNonNull(status, "Status");
        if (status == WorkoutSessionStatus.IN_PROGRESS && (finishedAt != null || reason != null)
                || status == WorkoutSessionStatus.CLOSED && (finishedAt == null || reason == null)) {
            throw new InvalidWorkoutSessionException("Estado e encerramento inconsistentes.");
        }
        if (finishedAt != null && (finishedAt.isBefore(startedAt) || finishedAt.isAfter(getExpiresAt()))) {
            throw new InvalidWorkoutSessionException("Horário de encerramento inválido.");
        }
        if (reason == WorkoutSessionCloseReason.AUTO_EXPIRED && !getExpiresAt().equals(finishedAt)) {
            throw new InvalidWorkoutSessionException("Expiração deve encerrar a sessão no horário limite.");
        }
        this.finishedAt = finishedAt;
        this.closeReason = reason;
        this.notes = notes;
        this.createdAt = requireNonNull(createdAt, "Data de criação");
        if (updatedAt != null) {
            validateUpdateTime(updatedAt, finishedAt != null ? finishedAt : createdAt);
        }
        this.updatedAt = updatedAt;
        if (version != null && version < 0) {
            throw new InvalidWorkoutSessionException("Versão inválida.");
        }
        this.version = version;
        this.activities = new ArrayList<>(requireNonNull(activities, "Atividades"));
        if (this.activities.stream().anyMatch(Objects::isNull)
                || this.activities.stream().map(a -> a.getSnapshot().orderIndex()).distinct().count() != this.activities.size()) {
            throw new InvalidWorkoutSessionException("Ordem das atividades deve ser única.");
        }
        if (this.activities.stream().map(WorkoutSessionActivity::getWorkoutActivityId)
                .filter(Objects::nonNull).distinct().count()
                != this.activities.stream().filter(a -> a.getWorkoutActivityId() != null).count()) {
            throw new InvalidWorkoutSessionException("Atividades de origem devem ser únicas.");
        }
        this.activities.sort(Comparator.comparing(a -> a.getSnapshot().orderIndex()));
    }

    public static WorkoutSession register(
            PersonId owner,
            WorkoutCycle cycle,
            WorkoutDay day,
            List<WorkoutSessionActivitySource> sources,
            Instant now
    ) {
        requireNonNull(owner, "Pessoa");
        requireNonNull(cycle, "Ciclo");
        requireNonNull(day, "Dia");
        requireNonNull(now, "Início");
        requireNonNull(sources, "Atividades de origem");
        if (cycle.getId() == null || day.getId() == null || !cycle.getPersonId().equals(owner)
                || !day.getWorkoutCycleId().equals(cycle.getId())) {
            throw new InvalidWorkoutSessionException("Planejamento de origem não pertence à pessoa ou ao ciclo.");
        }
        if (cycle.getWorkoutStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new InvalidWorkoutSessionException("Somente um ciclo em andamento permite iniciar sessão.");
        }
        if (sources.isEmpty()) {
            throw new InvalidWorkoutSessionException("O dia de treino deve possuir pelo menos uma atividade planejada.");
        }
        List<WorkoutSessionActivity> captured = new ArrayList<>();
        for (WorkoutSessionActivitySource source : sources) {
            requireNonNull(source, "Atividade de origem");
            if (!source.activity().getWorkoutDayId().equals(day.getId())) {
                throw new InvalidWorkoutSessionException("Atividade não pertence ao dia de origem.");
            }
            captured.add(WorkoutSessionActivity.register(source.activity(), source.exercise(), source.activityType(), now));
        }

        return new WorkoutSession(null, owner, cycle.getId(), day.getId(),
                new WorkoutSessionSnapshot(cycle.getName(), day.getTitle(), day.getWeekDay()),
                now.atZone(ZONE).toLocalDate(), now, null, WorkoutSessionStatus.IN_PROGRESS, null, null,
                now, null, null, captured);
    }

    public static WorkoutSession restore(
            WorkoutSessionId id,
            PersonId personId,
            WorkoutCycleId cycleId,
            WorkoutDayId dayId,
            WorkoutSessionSnapshot snapshot,
            LocalDate performedOn,
            Instant startedAt,
            Instant finishedAt,
            WorkoutSessionStatus status,
            WorkoutSessionCloseReason reason,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            Long version,
            List<WorkoutSessionActivity> activities
    ) {
        return new WorkoutSession(requireNonNull(id, "Id da sessão"), personId, cycleId, dayId, snapshot, performedOn,
                startedAt, finishedAt, status, reason, notes, createdAt, updatedAt,
                requireNonNull(version, "Versão"), activities);
    }

    public List<WorkoutSessionActivity> getActivities() {
        return List.copyOf(activities);
    }

    public Instant getExpiresAt() {
        return performedOn.plusDays(1).atTime(3, 0).atZone(ZONE).toInstant();
    }

    public void closeManually(PersonId actor, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        if (status != WorkoutSessionStatus.IN_PROGRESS) {
            throw new InvalidWorkoutSessionException("Sessão já encerrada.");
        }
        if (!expireIfDue(now)) {
            close(WorkoutSessionCloseReason.MANUAL, now, now);
        }
    }

    public boolean expireIfDue(Instant now) {
        requireNonNull(now, "Instante de referência");
        if (status == WorkoutSessionStatus.CLOSED || now.isBefore(getExpiresAt())) {
            return false;
        }
        requireUpdateTime(now);
        close(WorkoutSessionCloseReason.AUTO_EXPIRED, getExpiresAt(), now);

        return true;
    }

    public void updateNotes(PersonId actor, String notes, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        this.notes = notes;
        updatedAt = now;
    }

    public void updateActivityExecution(
            PersonId actor,
            WorkoutSessionActivityId activityId,
            Integer duration,
            BigDecimal distance,
            String notes,
            Instant updatedAt
    ) {
        requireOwner(actor);
        requireUpdateTime(updatedAt);
        activity(activityId).updateExecution(duration, distance, notes, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void addSet(
            PersonId actor,
            WorkoutSessionActivityId activityId,
            Integer number,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant updatedAt
    ) {
        requireOwner(actor);
        requireUpdateTime(updatedAt);
        activity(activityId).addSet(number, load, mode, reps, notes, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void updateSet(
            PersonId actor,
            WorkoutSessionActivityId activityId,
            WorkoutSessionSetId setId,
            BigDecimal load,
            ActualLoadMode mode,
            Integer reps,
            String notes,
            Instant updatedAt
    ) {
        requireOwner(actor);
        requireUpdateTime(updatedAt);
        activity(activityId).updateSet(setId, load, mode, reps, notes, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void removeSet(
            PersonId actor,
            WorkoutSessionActivityId activityId,
            WorkoutSessionSetId setId,
            Instant updatedAt
    ) {
        requireOwner(actor);
        requireUpdateTime(updatedAt);
        activity(activityId).removeSet(setId, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void changeActivityLoadMode(
            PersonId actor,
            WorkoutSessionActivityId activityId,
            ActualLoadMode mode,
            Instant updatedAt
    ) {
        requireOwner(actor);
        requireUpdateTime(updatedAt);
        activity(activityId).changeLoadMode(mode, updatedAt);
        this.updatedAt = updatedAt;
    }

    public void validateExecution() {
        activities.forEach(WorkoutSessionActivity::validateSets);
    }

    private WorkoutSessionActivity activity(WorkoutSessionActivityId activityId) {
        requireNonNull(activityId, "Id da atividade");
        return activities.stream()
                .filter(activity -> activityId.equals(activity.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidWorkoutSessionException("Atividade não pertence à sessão."));
    }

    private void requireOwner(PersonId actor) {
        if (!personId.equals(actor)) {
            throw new InvalidWorkoutSessionException("Somente o proprietário pode alterar a sessão.");
        }
    }

    private void requireUpdateTime(Instant now) {
        validateUpdateTime(now, updatedAt != null ? updatedAt : createdAt);
    }

    private void close(WorkoutSessionCloseReason reason, Instant finishedAt, Instant now) {
        status = WorkoutSessionStatus.CLOSED;
        closeReason = reason;
        this.finishedAt = finishedAt;
        updatedAt = now;
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidWorkoutSessionException(field + " é obrigatório.");
        }

        return value;
    }

    private void validateUpdateTime(Instant value, Instant createdAt) {
        requireNonNull(value, "Data de atualização");

        if (value.isBefore(createdAt)) {
            throw new InvalidWorkoutSessionException("Data de atualização anterior à criação.");
        }
    }
}
