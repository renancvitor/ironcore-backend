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
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.ironcore.domain.workoutsession.workoutsession.model.SessionValidation.*;

@Getter
public final class WorkoutSession {
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
    @Getter(lombok.AccessLevel.NONE)
    private final List<WorkoutSessionActivity> activities;

    private WorkoutSession(WorkoutSessionId id, PersonId personId, WorkoutCycleId cycleId, WorkoutDayId dayId,
                           WorkoutSessionSnapshot snapshot, LocalDate performedOn, Instant startedAt, Instant finishedAt,
                           WorkoutSessionStatus status, WorkoutSessionCloseReason reason, String notes,
                           Instant createdAt, Instant updatedAt, Long version, List<WorkoutSessionActivity> activities) {
        this.id = id;
        this.personId = required(personId, "Pessoa");
        this.workoutCycleId = cycleId;
        this.workoutDayId = dayId;
        this.snapshot = required(snapshot, "Snapshot da sessão");
        this.startedAt = required(startedAt, "Início");
        this.performedOn = required(performedOn, "Data local de início");
        if (!performedOn.equals(startedAt.atZone(ZONE).toLocalDate()))
            throw new InvalidWorkoutSessionException("Data de referência deve corresponder ao início no fuso da sessão.");
        this.status = required(status, "Status");
        if (status == WorkoutSessionStatus.IN_PROGRESS && (finishedAt != null || reason != null)
                || status == WorkoutSessionStatus.CLOSED && (finishedAt == null || reason == null))
            throw new InvalidWorkoutSessionException("Estado e encerramento inconsistentes.");
        if (finishedAt != null && (finishedAt.isBefore(startedAt) || finishedAt.isAfter(getExpiresAt())))
            throw new InvalidWorkoutSessionException("Horário de encerramento inválido.");
        if (reason == WorkoutSessionCloseReason.AUTO_EXPIRED && !getExpiresAt().equals(finishedAt))
            throw new InvalidWorkoutSessionException("Expiração deve encerrar a sessão no horário limite.");
        this.finishedAt = finishedAt;
        this.closeReason = reason;
        this.notes = notes;
        this.createdAt = required(createdAt, "Data de criação");
        if (!createdAt.equals(startedAt))
            throw new InvalidWorkoutSessionException("Criação deve corresponder ao início da sessão.");
        if (updatedAt != null) timestamp(updatedAt, finishedAt != null ? finishedAt : createdAt);
        this.updatedAt = updatedAt;
        if (version != null && version < 0)
            throw new InvalidWorkoutSessionException("Versão inválida.");
        this.version = version;
        this.activities = new ArrayList<>(required(activities, "Atividades"));
        if (this.activities.stream().anyMatch(Objects::isNull)
                || this.activities.stream().map(a -> a.getSnapshot().orderIndex()).distinct().count() != this.activities.size())
            throw new InvalidWorkoutSessionException("Ordem das atividades deve ser única.");
        this.activities.sort(Comparator.comparing(a -> a.getSnapshot().orderIndex()));
    }

    public static WorkoutSession start(PersonId owner, WorkoutCycle cycle, WorkoutDay day,
                                       List<WorkoutSessionActivitySource> sources, Instant now) {
        required(owner, "Pessoa");
        required(cycle, "Ciclo");
        required(day, "Dia");
        required(now, "Início");
        required(sources, "Atividades de origem");
        if (cycle.getId() == null || day.getId() == null || !cycle.getPersonId().equals(owner)
                || !day.getWorkoutCycleId().equals(cycle.getId()))
            throw new InvalidWorkoutSessionException("Planejamento de origem não pertence à pessoa ou ao ciclo.");
        if (cycle.getWorkoutStatus() != WorkoutStatus.IN_PROGRESS)
            throw new InvalidWorkoutSessionException("Somente um ciclo em andamento permite iniciar sessão.");
        List<WorkoutSessionActivity> captured = new ArrayList<>();
        for (WorkoutSessionActivitySource source : sources) {
            required(source, "Atividade de origem");
            if (!source.activity().getWorkoutDayId().equals(day.getId()))
                throw new InvalidWorkoutSessionException("Atividade não pertence ao dia de origem.");
            captured.add(WorkoutSessionActivity.capture(source.activity(), source.exercise(), source.activityType(), now));
        }

        return new WorkoutSession(null, owner, cycle.getId(), day.getId(),
                new WorkoutSessionSnapshot(cycle.getName(), day.getTitle(), day.getWeekDay()),
                now.atZone(ZONE).toLocalDate(), now, null, WorkoutSessionStatus.IN_PROGRESS, null, null,
                now, null, null, captured);
    }

    public static WorkoutSession restore(WorkoutSessionId id, PersonId personId, WorkoutCycleId cycleId, WorkoutDayId dayId,
                                         WorkoutSessionSnapshot snapshot, LocalDate performedOn, Instant startedAt,
                                         Instant finishedAt, WorkoutSessionStatus status, WorkoutSessionCloseReason reason,
                                         String notes, Instant createdAt, Instant updatedAt, Long version,
                                         List<WorkoutSessionActivity> activities) {
        return new WorkoutSession(required(id, "Id da sessão"), personId, cycleId, dayId, snapshot, performedOn,
                startedAt, finishedAt, status, reason, notes, createdAt, updatedAt,
                required(version, "Versão"), activities);
    }

    public List<WorkoutSessionActivity> getActivities() { return List.copyOf(activities); }

    public Instant getExpiresAt() {
        return performedOn.plusDays(1).atTime(3, 0).atZone(ZONE).toInstant();
    }

    public void closeManually(PersonId actor, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        if (status != WorkoutSessionStatus.IN_PROGRESS)
            throw new InvalidWorkoutSessionException("Sessão já encerrada.");
        if (!expireIfDue(now)) close(WorkoutSessionCloseReason.MANUAL, now, now);
    }

    public boolean expireIfDue(Instant now) {
        required(now, "Instante de referência");
        if (status == WorkoutSessionStatus.CLOSED || now.isBefore(getExpiresAt())) return false;
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

    public void updateActivityExecution(PersonId actor, int orderIndex, Integer duration, BigDecimal distance,
                                        String notes, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        replaceActivity(activity(orderIndex).updateExecution(duration, distance, notes, now));
        updatedAt = now;
    }

    public void recordSet(PersonId actor, int orderIndex, Integer number, BigDecimal load, ActualLoadMode mode,
                          Integer reps, String notes, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        replaceActivity(activity(orderIndex).recordSet(number, load, mode, reps, notes, now));
        updatedAt = now;
    }

    public void removeSet(PersonId actor, int orderIndex, Integer number, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        replaceActivity(activity(orderIndex).removeSet(number, now));
        updatedAt = now;
    }

    public void changeActivityLoadMode(PersonId actor, int orderIndex, ActualLoadMode mode, Instant now) {
        requireOwner(actor);
        requireUpdateTime(now);
        replaceActivity(activity(orderIndex).changeLoadMode(mode, now));
        updatedAt = now;
    }

    private void replaceActivity(WorkoutSessionActivity updated) {
        for (int index = 0; index < activities.size(); index++) {
            if (activities.get(index).getSnapshot().orderIndex().equals(updated.getSnapshot().orderIndex())) {
                activities.set(index, updated);
                return;
            }
        }
        throw new InvalidWorkoutSessionException("Atividade não pertence à sessão.");
    }

    private WorkoutSessionActivity activity(int orderIndex) {
        return activities.stream().filter(a -> a.getSnapshot().orderIndex() == orderIndex).findFirst()
                .orElseThrow(() -> new InvalidWorkoutSessionException("Atividade não pertence à sessão."));
    }

    private void requireOwner(PersonId actor) {
        if (!personId.equals(actor))
            throw new InvalidWorkoutSessionException("Somente o proprietário pode alterar a sessão.");
    }

    private void requireUpdateTime(Instant now) {
        timestamp(now, updatedAt != null ? updatedAt : createdAt);
    }

    private void close(WorkoutSessionCloseReason reason, Instant finishedAt, Instant now) {
        status = WorkoutSessionStatus.CLOSED;
        closeReason = reason;
        this.finishedAt = finishedAt;
        updatedAt = now;
    }
}
