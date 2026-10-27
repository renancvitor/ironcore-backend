package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.mapper.ActivityTypeMapper;
import com.ironcore.infrastructure.persistence.exercise.mapper.ExerciseMapper;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.mapper.WorkoutActivityMapper;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.repository.WorkoutActivityJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository.WorkoutDayJpaRepository;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper.WorkoutSessionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionAdapter implements WorkoutSessionRepository {

    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;
    private final PersonJpaRepository personJpaRepository;
    private final WorkoutDayJpaRepository workoutDayJpaRepository;
    private final WorkoutActivityJpaRepository workoutActivityJpaRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public WorkoutSession save(WorkoutSession workoutSession) {
        workoutSession.validateExecution();

        try {
            WorkoutSessionEntity entity;
            if (workoutSession.getId() == null) {
                validatePlanningSnapshot(workoutSession);
                entity = WorkoutSessionMapper.toEntity(workoutSession,
                        personJpaRepository.getReferenceById(workoutSession.getPersonId().value()));
                entity = workoutSessionJpaRepository.saveAndFlush(entity);
            } else {
                entity = workoutSessionJpaRepository.findByIdAndPerson_Id(
                                workoutSession.getId().value(), workoutSession.getPersonId().value())
                        .orElseThrow(() -> new WorkoutSessionConflictException(
                                "Sessão indisponível para o proprietário."));
                if (!Objects.equals(entity.getVersion(), workoutSession.getVersion())) {
                    throw new WorkoutSessionConflictException("Sessão foi alterada por outra operação.");
                }

                WorkoutSessionMapper.applyExecution(workoutSession, entity);
                // Version the entire aggregate, including edits restricted to activities or sets.
                entityManager.lock(entity, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                workoutSessionJpaRepository.flush();
            }

            return WorkoutSessionMapper.toDomain(entity);
        } catch (WorkoutSessionConflictException | DataMappingException exception) {
            throw exception;
        } catch (OptimisticLockingFailureException | OptimisticLockException | StaleStateException exception) {
            throw new WorkoutSessionConflictException("Sessão foi alterada por outra operação.", exception);
        } catch (DataIntegrityViolationException exception) {
            throw translateIntegrityViolation(exception);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir sessão de treino.", exception);
        }
    }

    @Override
    public Optional<WorkoutSession> findByIdAndPersonId(WorkoutSessionId id, PersonId personId) {
        Optional<WorkoutSessionEntity> entity;
        try {
            entity = workoutSessionJpaRepository.findByIdAndPerson_Id(id.value(), personId.value());
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar sessão do proprietário.", exception);
        }

        return entity.map(WorkoutSessionMapper::toDomain);
    }

    @Override
    public Optional<WorkoutSession> findInProgressByPersonId(PersonId personId) {
        Optional<WorkoutSessionEntity> entity;
        try {
            entity = workoutSessionJpaRepository.findByPerson_IdAndStatus(
                    personId.value(), WorkoutSessionStatus.IN_PROGRESS);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar sessão aberta do proprietário.", exception);
        }

        return entity.map(WorkoutSessionMapper::toDomain);
    }

    private void validatePlanningSnapshot(WorkoutSession workoutSession) {
        WorkoutDayEntity day = workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(
                        workoutSession.getWorkoutDayId().value(), workoutSession.getPersonId().value())
                .orElseThrow(() -> new WorkoutSessionConflictException("Planejamento de origem indisponível."));
        if (!day.getWorkoutCycle().getId().equals(workoutSession.getWorkoutCycleId().value())
                || day.getWorkoutCycle().getWorkoutStatus() != WorkoutStatus.IN_PROGRESS
                || !new WorkoutSessionSnapshot(day.getWorkoutCycle().getName(), day.getTitle(), day.getWeekDay())
                .equals(workoutSession.getSnapshot())) {
            throw new WorkoutSessionConflictException("Planejamento de origem foi alterado. Recarregue a ficha.");
        }

        // #311 must load the complete prescription; persistence rejects omissions or stale captures.
        List<WorkoutActivityEntity> plannedActivities = workoutActivityJpaRepository
                .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(
                        workoutSession.getPersonId().value(), day.getId());
        Map<Long, WorkoutActivityEntity> byId = plannedActivities.stream()
                .collect(Collectors.toMap(WorkoutActivityEntity::getId, Function.identity()));
        if (byId.size() != workoutSession.getActivities().size()) {
            throw new WorkoutSessionConflictException("Snapshot não corresponde à ficha completa do planejamento.");
        }
        for (WorkoutSessionActivity activity : workoutSession.getActivities()) {
            WorkoutActivityEntity planned = byId.get(activity.getWorkoutActivityId().value());
            if (planned == null || !planned.getExercise().getId().equals(activity.getExerciseId().value())) {
                throw new WorkoutSessionConflictException("Atividade de origem foi alterada.");
            }
            WorkoutSessionActivitySnapshot expected = WorkoutSessionActivitySnapshot.capture(
                    WorkoutActivityMapper.toDomain(planned), ExerciseMapper.toDomain(planned.getExercise()),
                    ActivityTypeMapper.toDomain(planned.getExercise().getActivityType()));
            if (!expected.equals(activity.getSnapshot())) {
                throw new WorkoutSessionConflictException("Prescrição foi alterada. Recarregue a ficha.");
            }
        }
    }

    private RuntimeException translateIntegrityViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraint = violation.getConstraintName();
                if ("uk_workout_sessions_person_in_progress".equals(constraint)) {
                    return new WorkoutSessionConflictException("Já existe uma sessão em andamento para a pessoa.", exception);
                }
                if ("uk_workout_session_sets_number".equals(constraint)
                        || "uk_workout_session_activities_order".equals(constraint)) {
                    return new WorkoutSessionConflictException("Registro de sessão duplicado.", exception);
                }
                if ("workout_sessions_workout_cycle_id_fkey".equals(constraint)
                        || "workout_sessions_workout_day_id_fkey".equals(constraint)
                        || "workout_session_activities_workout_activity_id_fkey".equals(constraint)
                        || "workout_session_activities_exercise_id_fkey".equals(constraint)) {
                    return new WorkoutSessionConflictException("Origem alterada durante a abertura da sessão.", exception);
                }
            }
        }
        return new PersistenceException("Falha de integridade ao persistir sessão de treino.", exception);
    }
}
