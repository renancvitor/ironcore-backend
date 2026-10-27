package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper.WorkoutSessionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionAdapter implements WorkoutSessionRepository {

    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;
    private final PersonJpaRepository personJpaRepository;
    private final EntityManager entityManager;
    private final WorkoutSessionPlanningQueryAdapter workoutSessionPlanningQueryAdapter;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WorkoutSession save(WorkoutSession workoutSession) {
        workoutSession.validateExecution();

        try {
            WorkoutSessionEntity entity;
            if (workoutSession.getId() == null) {
                WorkoutSessionPlanningProjection planning = workoutSessionPlanningQueryAdapter.lockByDayIdAndPersonId(
                                workoutSession.getWorkoutDayId(), workoutSession.getPersonId())
                        .orElseThrow(() -> new WorkoutSessionConflictException("Planejamento de origem indisponível."));
                validatePlanningSnapshot(workoutSession, planning);
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

                entityManager.lock(entity, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                removeDeletedSets(workoutSession, entity);
                WorkoutSessionMapper.applyExecution(workoutSession, entity);
                workoutSessionJpaRepository.flush();
            }

            return WorkoutSessionMapper.toDomain(entity);
        } catch (WorkoutSessionConflictException | InvalidWorkoutSessionException | DataMappingException exception) {
            throw exception;
        } catch (ConcurrencyFailureException | OptimisticLockException | StaleStateException
                 | PessimisticLockException | LockTimeoutException | LockAcquisitionException exception) {
            throw new WorkoutSessionConflictException("Sessão foi alterada por outra operação.", exception);
        } catch (DataIntegrityViolationException exception) {
            throw translateIntegrityViolation(exception);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir sessão de treino.", exception);
        }
    }

    private void validatePlanningSnapshot(WorkoutSession session, WorkoutSessionPlanningProjection planning) {
        if (!planning.personId().equals(session.getPersonId())
                || !planning.cycleId().equals(session.getWorkoutCycleId())
                || !planning.dayId().equals(session.getWorkoutDayId())
                || planning.status() != WorkoutStatus.IN_PROGRESS
                || !planning.snapshot().equals(session.getSnapshot())) {
            throw new WorkoutSessionConflictException("Planejamento de origem foi alterado. Recarregue a ficha.");
        }
        if (session.getActivities().isEmpty()) {
            throw new InvalidWorkoutSessionException("O dia de treino deve possuir pelo menos uma atividade planejada.");
        }

        Map<Long, WorkoutSessionPlanningProjection.Activity> byId = planning.activities().stream()
                .collect(Collectors.toMap(activity -> activity.activityId().value(), Function.identity()));
        if (byId.size() != session.getActivities().size()) {
            throw new WorkoutSessionConflictException("Snapshot não corresponde à ficha completa do planejamento.");
        }
        for (WorkoutSessionActivity activity : session.getActivities()) {
            WorkoutSessionPlanningProjection.Activity planned = byId.get(activity.getWorkoutActivityId().value());
            if (planned == null || !planned.exerciseId().equals(activity.getExerciseId())) {
                throw new WorkoutSessionConflictException("Atividade de origem foi alterada.");
            }
            if (!planned.snapshot().equals(activity.getSnapshot())) {
                throw new WorkoutSessionConflictException("Prescrição foi alterada. Recarregue a ficha.");
            }
        }
    }

    private void removeDeletedSets(WorkoutSession source, WorkoutSessionEntity entity) {
        boolean removed = false;
        for (WorkoutSessionActivity activity : source.getActivities()) {
            if (activity.getId() == null) {
                continue;
            }
            Set<Long> retained = activity.getSets().stream().filter(set -> set.getId() != null)
                    .map(set -> set.getId().value()).collect(Collectors.toSet());
            for (var target : entity.getActivities()) {
                if (activity.getId().value().equals(target.getId())) {
                    removed |= target.getSets().removeIf(set -> !retained.contains(set.getId()));
                }
            }
        }
        if (removed) {
            workoutSessionJpaRepository.flush();
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

    @Override
    public List<PersonId> findInProgressPersonIdsAfter(PersonId afterPersonId, int limit) {
        try {
            return workoutSessionJpaRepository.findInProgressPersonIdsAfter(
                            WorkoutSessionStatus.IN_PROGRESS,
                            afterPersonId == null ? null : afterPersonId.value(),
                            PageRequest.of(0, limit))
                    .stream().map(PersonId::new).toList();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar pessoas com sessão em andamento.", exception);
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
