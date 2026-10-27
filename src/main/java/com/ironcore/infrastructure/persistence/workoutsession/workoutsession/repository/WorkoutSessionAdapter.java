package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.persistence.ValidatedWorkoutSession;
import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionCreationPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionAdapter implements WorkoutSessionRepository, WorkoutSessionCreationPort {

    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;
    private final PersonJpaRepository personJpaRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public WorkoutSession save(WorkoutSession workoutSession) {
        if (workoutSession.getId() == null) {
            throw new InvalidWorkoutSessionException("Sessão deve ser validada antes da criação.");
        }
        return persist(workoutSession);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public WorkoutSession insert(ValidatedWorkoutSession validatedSession) {
        WorkoutSession session = validatedSession.getSession();
        if (session.getId() != null) {
            throw new InvalidWorkoutSessionException("Sessão já possui identificador.");
        }
        return persist(session);
    }

    private WorkoutSession persist(WorkoutSession workoutSession) {
        workoutSession.validateExecution();

        try {
            WorkoutSessionEntity entity;
            if (workoutSession.getId() == null) {
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
