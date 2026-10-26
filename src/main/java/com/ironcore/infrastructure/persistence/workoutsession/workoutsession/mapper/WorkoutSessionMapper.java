package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.mapper.WorkoutSessionActivityMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class WorkoutSessionMapper {
    private WorkoutSessionMapper() {}

    public static WorkoutSessionEntity toEntity(WorkoutSession source, PersonEntity owner) {
        try {
            if (!source.getPersonId().value().equals(owner.getId()))
                throw new DataMappingException("Proprietário divergente.");
            WorkoutSessionEntity entity = new WorkoutSessionEntity();
            entity.setId(source.getId() == null ? null : source.getId().value());
            entity.setPerson(owner);
            entity.setWorkoutCycleId(source.getWorkoutCycleId() == null ? null : source.getWorkoutCycleId().value());
            entity.setWorkoutDayId(source.getWorkoutDayId() == null ? null : source.getWorkoutDayId().value());
            entity.setWorkoutCycleNameSnapshot(source.getSnapshot().workoutCycleName());
            entity.setWorkoutDayTitleSnapshot(source.getSnapshot().workoutDayTitle());
            entity.setWeekDaySnapshot(source.getSnapshot().weekDay());
            entity.setPerformedOn(source.getPerformedOn());
            entity.setStartedAt(source.getStartedAt());
            entity.setCreatedAt(source.getCreatedAt());
            entity.setVersion(source.getVersion());
            applySessionExecution(source, entity);
            source.getActivities().forEach(a -> entity.getActivities().add(WorkoutSessionActivityMapper.toEntity(a, entity)));
            return entity;
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao mapear sessão para persistência.", ex);
        }
    }

    public static WorkoutSession toDomain(WorkoutSessionEntity entity) {
        try {
            return WorkoutSession.restore(new WorkoutSessionId(entity.getId()), new PersonId(entity.getPerson().getId()),
                    entity.getWorkoutCycleId() == null ? null : new WorkoutCycleId(entity.getWorkoutCycleId()),
                    entity.getWorkoutDayId() == null ? null : new WorkoutDayId(entity.getWorkoutDayId()),
                    new WorkoutSessionSnapshot(entity.getWorkoutCycleNameSnapshot(), entity.getWorkoutDayTitleSnapshot(),
                            entity.getWeekDaySnapshot()),
                    entity.getPerformedOn(), entity.getStartedAt(), entity.getFinishedAt(), entity.getStatus(),
                    entity.getCloseReason(), entity.getNotes(), entity.getCreatedAt(), entity.getUpdatedAt(), entity.getVersion(),
                    entity.getActivities().stream().map(WorkoutSessionActivityMapper::toDomain).toList());
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao restaurar sessão persistida.", ex);
        }
    }

    public static void applyExecution(WorkoutSession source, WorkoutSessionEntity entity) {
        WorkoutSession persisted = toDomain(entity);
        if (!persisted.getPersonId().equals(source.getPersonId())
                || !persisted.getSnapshot().equals(source.getSnapshot())
                || !persisted.getStartedAt().equals(source.getStartedAt())
                || !persisted.getPerformedOn().equals(source.getPerformedOn())
                || !persisted.getCreatedAt().equals(source.getCreatedAt())
                || !Objects.equals(persisted.getWorkoutCycleId(), source.getWorkoutCycleId())
                || !Objects.equals(persisted.getWorkoutDayId(), source.getWorkoutDayId()))
            throw new DataMappingException("Proprietário, origem e snapshot da sessão são imutáveis.");

        if (persisted.getStatus() == WorkoutSessionStatus.CLOSED
                && (source.getStatus() != WorkoutSessionStatus.CLOSED
                || source.getCloseReason() != persisted.getCloseReason()
                || !Objects.equals(source.getFinishedAt(), persisted.getFinishedAt())))
            throw new DataMappingException("Encerramento da sessão é imutável.");

        Map<Long, WorkoutSessionActivityEntity> existing = entity.getActivities().stream()
                .collect(Collectors.toMap(WorkoutSessionActivityEntity::getId, Function.identity()));

        if (existing.size() != source.getActivities().size())
            throw new DataMappingException("Composição histórica da sessão é imutável.");

        Set<Long> seen = new HashSet<>();

        for (WorkoutSessionActivity activity : source.getActivities()) {
            if (activity.getId() == null || !seen.add(activity.getId().value())
                    || !existing.containsKey(activity.getId().value()))
                throw new DataMappingException("Atividade não pertence à sessão.");

            WorkoutSessionActivityEntity target = existing.get(activity.getId().value());

            if (!Objects.equals(target.getWorkoutActivityId(),
                    activity.getWorkoutActivityId() == null ? null : activity.getWorkoutActivityId().value())
                    || !Objects.equals(target.getExerciseId(),
                    activity.getExerciseId() == null ? null : activity.getExerciseId().value()))
                throw new DataMappingException("Origem da atividade é imutável.");

            WorkoutSessionActivityMapper.applyExecution(activity, target);
        }

        applySessionExecution(source, entity);
    }

    private static void applySessionExecution(WorkoutSession source, WorkoutSessionEntity entity) {
        entity.setStatus(source.getStatus());
        entity.setCloseReason(source.getCloseReason());
        entity.setFinishedAt(source.getFinishedAt());
        entity.setNotes(source.getNotes());
        entity.setUpdatedAt(source.getUpdatedAt());
    }
}
