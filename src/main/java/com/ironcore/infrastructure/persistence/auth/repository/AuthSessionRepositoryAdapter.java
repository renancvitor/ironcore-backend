package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import com.ironcore.infrastructure.persistence.auth.mapper.AuthSessionMapper;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuthSessionRepositoryAdapter implements AuthSessionRepository {

    private final AuthSessionJpaRepository authSessionJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<UserId> findUserIdById(AuthSessionId id) {
        try {
            return authSessionJpaRepository.findUserIdById(id.value()).map(UserId::new);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar proprietário da sessão.", exception);
        }
    }

    @Override
    public AuthSession save(AuthSession session) {
        AuthSessionEntity entity;
        try {
            UserEntity userReference = userJpaRepository.getReferenceById(session.getUserId().value());
            entity = Objects.requireNonNull(
                    AuthSessionMapper.toEntity(session, userReference),
                    "Não foi possível encontrar entidade."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao obter referência do usuário.", exception);
        }

        AuthSessionEntity savedEntity;
        try {
            savedEntity = Objects.requireNonNull(
                    authSessionJpaRepository.save(entity),
                    "Não foi possível persistir entidade."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir sessão de autenticação.", exception);
        }

        try {
            return AuthSessionMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter sessão para entidade.", exception);
        }
    }

    @Override
    public Optional<AuthSession> findByIdAndUserId(AuthSessionId id, UserId userId) {
        Optional<AuthSessionEntity> entity;
        try {
            UUID idValue = Objects.requireNonNull(
                    id.value(),
                    "Id da sessão não pode ser nulo."
            );
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = authSessionJpaRepository.findByIdAndUser_Id(idValue, userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar sessão por usuário.", exception);
        }

        try {
            return entity.map(AuthSessionMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter sessão para domínio.", exception);
        }
    }

    @Override
    public int revokeAllByUserId(UserId userId, Instant revokedAt) {
        List<AuthSessionEntity> entities;

        try {
            entities = authSessionJpaRepository.findAllByUser_IdAndRevokedAtIsNull(
                    userId.value()
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao consultar sessões do usuário.", exception
            );
        }

        for (AuthSessionEntity entity : entities) {
            AuthSession session = AuthSessionMapper.toDomain(entity);
            session.revoke(revokedAt);
            entity.setRevokedAt(session.getRevokedAt());
        }

        try {
            authSessionJpaRepository.saveAll(entities);
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao revogar sessões do usuário.", exception
            );
        }

        return entities.size();
    }
}
