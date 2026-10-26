package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.infrastructure.exception.*;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper.WorkoutSessionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionAdapter implements WorkoutSessionRepository {
    private final WorkoutSessionJpaRepository jpaRepository;
    private final PersonJpaRepository personRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public WorkoutSession save(WorkoutSession session) {
        try {
            WorkoutSessionEntity entity;
            if (session.getId() == null) {
                entity = WorkoutSessionMapper.toEntity(session,
                        personRepository.getReferenceById(session.getPersonId().value()));
                entity = jpaRepository.saveAndFlush(entity);
            } else {
                entity = jpaRepository.findOwnedForUpdate(session.getId().value(), session.getPersonId().value())
                        .orElseThrow(() -> new PersistenceException("Sessão não encontrada para o proprietário."));
                if (!Objects.equals(entity.getVersion(), session.getVersion()))
                    throw new OptimisticLockingFailureException("Sessão foi alterada por outra operação.");
                WorkoutSessionMapper.applyExecution(session, entity);
                entityManager.lock(entity, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                jpaRepository.flush();
            }
            return WorkoutSessionMapper.toDomain(entity);
        } catch (DataMappingException | PersistenceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new PersistenceException("Falha ao persistir sessão de treino.", ex);
        }
    }

    @Override
    public Optional<WorkoutSession> findByIdAndPersonId(WorkoutSessionId id, PersonId personId) {
        try {
            return jpaRepository.findByIdAndPerson_Id(id.value(), personId.value()).map(WorkoutSessionMapper::toDomain);
        } catch (DataMappingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new PersistenceException("Falha ao buscar sessão do proprietário.", ex);
        }
    }

    @Override
    public Optional<WorkoutSession> findInProgressByPersonId(PersonId personId) {
        try {
            return jpaRepository.findByPerson_IdAndStatus(personId.value(), WorkoutSessionStatus.IN_PROGRESS)
                    .map(WorkoutSessionMapper::toDomain);
        } catch (DataMappingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new PersistenceException("Falha ao buscar sessão aberta do proprietário.", ex);
        }
    }
}
