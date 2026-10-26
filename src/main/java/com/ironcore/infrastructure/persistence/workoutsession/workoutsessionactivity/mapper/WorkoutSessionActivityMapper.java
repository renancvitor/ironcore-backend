package com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.mapper;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;
import com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity.WorkoutSessionSetEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.mapper.WorkoutSessionSetMapper;

import java.util.*;

public final class WorkoutSessionActivityMapper {
    private WorkoutSessionActivityMapper() {}

    public static WorkoutSessionActivityEntity toEntity(WorkoutSessionActivity source, WorkoutSessionEntity parent) {
        try {
            WorkoutSessionActivityEntity entity = new WorkoutSessionActivityEntity();
            entity.setId(source.getId() == null ? null : source.getId().value());
            entity.setSession(parent);
            entity.setWorkoutActivityId(source.getWorkoutActivityId() == null ? null : source.getWorkoutActivityId().value());
            entity.setExerciseId(source.getExerciseId() == null ? null : source.getExerciseId().value());
            entity.setExerciseNameSnapshot(source.getSnapshot().exerciseName());
            entity.setActivityTypeSnapshot(source.getSnapshot().activityType());
            entity.setOrderIndexSnapshot(source.getSnapshot().orderIndex());
            entity.setPlannedSetsSnapshot(source.getSnapshot().plannedSets());
            entity.setPlannedRepRangeMinSnapshot(source.getSnapshot().plannedRepRangeMin());
            entity.setPlannedRepRangeMaxSnapshot(source.getSnapshot().plannedRepRangeMax());
            entity.setPlannedTargetLoadKgSnapshot(source.getSnapshot().plannedTargetLoadKg());
            entity.setPlannedTargetLoadTextSnapshot(source.getSnapshot().plannedTargetLoadText());
            entity.setPlannedDurationMinutesSnapshot(source.getSnapshot().plannedDurationMinutes());
            entity.setPlannedDistanceKmSnapshot(source.getSnapshot().plannedDistanceKm());
            entity.setPlannedIntensityTextSnapshot(source.getSnapshot().plannedIntensityText());
            entity.setPlannedRestSecondsSnapshot(source.getSnapshot().plannedRestSeconds());
            entity.setPlannedNotesSnapshot(source.getSnapshot().plannedNotes());
            entity.setCreatedAt(source.getCreatedAt());
            entity.setActualDurationMinutes(source.getActualDurationMinutes());
            entity.setActualDistanceKm(source.getActualDistanceKm());
            entity.setNotes(source.getNotes());
            entity.setUpdatedAt(source.getUpdatedAt());
            source.getSets().forEach(s -> entity.getSets().add(WorkoutSessionSetMapper.toEntity(s, entity)));
            return entity;
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao mapear atividade da sessão para persistência.", ex);
        }
    }

    public static WorkoutSessionActivity toDomain(WorkoutSessionActivityEntity entity) {
        try {
            return WorkoutSessionActivity.restore(new WorkoutSessionActivityId(entity.getId()),
                    entity.getWorkoutActivityId() == null ? null : new WorkoutActivityId(entity.getWorkoutActivityId()),
                    entity.getExerciseId() == null ? null : new ExerciseId(entity.getExerciseId()),
                    new WorkoutSessionActivitySnapshot(
                            entity.getExerciseNameSnapshot(),
                            entity.getActivityTypeSnapshot(),
                            entity.getOrderIndexSnapshot(),
                            entity.getPlannedSetsSnapshot(),
                            entity.getPlannedRepRangeMinSnapshot(),
                            entity.getPlannedRepRangeMaxSnapshot(),
                            entity.getPlannedTargetLoadKgSnapshot(),
                            entity.getPlannedTargetLoadTextSnapshot(),
                            entity.getPlannedDurationMinutesSnapshot(),
                            entity.getPlannedDistanceKmSnapshot(),
                            entity.getPlannedIntensityTextSnapshot(),
                            entity.getPlannedRestSecondsSnapshot(),
                            entity.getPlannedNotesSnapshot()),
                    entity.getActualDurationMinutes(), entity.getActualDistanceKm(), entity.getNotes(),
                    entity.getCreatedAt(), entity.getUpdatedAt(),
                    entity.getSets().stream().map(WorkoutSessionSetMapper::toDomain).toList());
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao restaurar atividade da sessão persistida.", ex);
        }
    }

    public static void applyExecution(WorkoutSessionActivity source, WorkoutSessionActivityEntity entity) {
        WorkoutSessionActivity persisted = toDomain(entity);
        if (!persisted.getSnapshot().equals(source.getSnapshot())
                || !persisted.getCreatedAt().equals(source.getCreatedAt()))
            throw new DataMappingException("Snapshot e criação da atividade são imutáveis.");
        entity.setActualDurationMinutes(source.getActualDurationMinutes());
        entity.setActualDistanceKm(source.getActualDistanceKm());
        entity.setNotes(source.getNotes());
        entity.setUpdatedAt(source.getUpdatedAt());

        Map<Long, WorkoutSessionSetEntity> existing = new HashMap<>();
        entity.getSets().forEach(s -> existing.put(s.getId(), s));
        Set<Long> retained = new HashSet<>();

        for (WorkoutSessionSet set : source.getSets()) {
            if (set.getId() == null) {
                if (entity.getSets().stream().anyMatch(s -> s.getSetNumber().equals(set.getSetNumber())))
                    throw new DataMappingException("Série existente deve preservar seu identificador.");

                entity.getSets().add(WorkoutSessionSetMapper.toEntity(set, entity));
            } else {
                WorkoutSessionSetEntity target = existing.get(set.getId().value());

                if (target == null || !retained.add(target.getId())
                        || !target.getSetNumber().equals(set.getSetNumber())
                        || !target.getCreatedAt().equals(set.getCreatedAt()))
                    throw new DataMappingException("Série não pertence à atividade ou possui vínculo alterado.");

                WorkoutSessionSetMapper.applyExecution(set, target);
            }
        }

        entity.getSets().removeIf(s -> s.getId() != null && !retained.contains(s.getId()));
    }
}
