package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import com.ironcore.infrastructure.persistence.userbodymetrics.mapper.UserBodyMetricsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserBodyMetricsRepositoryAdapter  implements UserBodyMetricsRepository {

    private final UserBodyMetricsJpaRepository userBodyMetricsJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserBodyMetrics save(UserBodyMetrics userBodyMetrics) {
        UserBodyMetricsEntity entity;
        try {
            UserEntity userReference = userJpaRepository.getReferenceById(
                    userBodyMetrics.getUserId().value()
            );

            entity = Objects.requireNonNull(
                    UserBodyMetricsMapper.toEntity(userBodyMetrics, userReference),
                    "UserBodyMetrics retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        UserBodyMetricsEntity saveEntity;
        try {
            saveEntity = Objects.requireNonNull(
                    userBodyMetricsJpaRepository.save(entity),
                    "UserBodyMetricsMapper retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir entidade.", exception);
        }

        try {
            return UserBodyMetricsMapper.toDomain(saveEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter persistido para domain.", exception);
        }
    }

    @Override
    public Optional<UserBodyMetrics> findById(UserBodyMetricsId userBodyMetricsId) {
        Optional<UserBodyMetricsEntity> entity;
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    userBodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            entity = userBodyMetricsJpaRepository.findById(userBodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id.", exception);
        }

        try {
            return entity.map(UserBodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter id encontrado para domínio.", exception);
        }
    }

    @Override
    public Optional<UserBodyMetrics> findByIdAndUserId(UserBodyMetricsId userBodyMetricsId, UserId userId) {
        Optional<UserBodyMetricsEntity> entity;
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    userBodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = userBodyMetricsJpaRepository.findByIdAndUser_Id(userBodyMetricsIdValue, userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id e usuário.", exception);
        }

        try {
            return entity.map(UserBodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter métricas corporais encontradas para domínio.",
                    exception
            );
        }
    }

    @Override
    public Optional<UserBodyMetrics> findLatestByUserId(UserId userId) {
        Optional<UserBodyMetricsEntity> entity;
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = userBodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar último registro pelo usuário.", exception);
        }

        try {
            return entity.map(UserBodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter métricas corporais encontradas para domínio.",
                    exception
            );
        }
    }

    @Override
    public List<UserBodyMetrics> findByUserIdOrderByMeasuredAtDesc(UserId userId) {
        List<UserBodyMetricsEntity> entity;
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDesc(userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao buscar registros pelo usuário com data de medição descendente.",
                    exception
            );
        }

        try {
            return entity.stream().map(UserBodyMetricsMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter métricas corporais encontradas para domínio.",
                    exception
            );
        }
    }

    @Override
    public void deleteById(UserBodyMetricsId userBodyMetricsId) {
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    userBodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            userBodyMetricsJpaRepository.deleteById(userBodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao excluir métricas corporais por id.", exception);
        }
    }
}
