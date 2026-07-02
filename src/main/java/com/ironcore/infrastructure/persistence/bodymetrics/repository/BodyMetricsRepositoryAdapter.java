package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import com.ironcore.infrastructure.persistence.bodymetrics.mapper.BodyMetricsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BodyMetricsRepositoryAdapter implements BodyMetricsRepository {

    private final BodyMetricsJpaRepository bodyMetricsJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public BodyMetrics save(BodyMetrics bodyMetrics) {
        BodyMetricsEntity entity;
        try {
            UserEntity userReference = userJpaRepository.getReferenceById(
                    bodyMetrics.getUserId().value()
            );

            entity = Objects.requireNonNull(
                    BodyMetricsMapper.toEntity(bodyMetrics, userReference),
                    "UserBodyMetrics retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        BodyMetricsEntity saveEntity;
        try {
            saveEntity = Objects.requireNonNull(
                    bodyMetricsJpaRepository.save(entity),
                    "UserBodyMetricsMapper retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir entidade.", exception);
        }

        try {
            return BodyMetricsMapper.toDomain(saveEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter persistido para domain.", exception);
        }
    }

    @Override
    public Optional<BodyMetrics> findById(BodyMetricsId bodyMetricsId) {
        Optional<BodyMetricsEntity> entity;
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findById(userBodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id.", exception);
        }

        try {
            return entity.map(BodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter id encontrado para domínio.", exception);
        }
    }

    @Override
    public Optional<BodyMetrics> findByIdAndUserId(BodyMetricsId bodyMetricsId, UserId userId) {
        Optional<BodyMetricsEntity> entity;
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findByIdAndUser_Id(userBodyMetricsIdValue, userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id e usuário.", exception);
        }

        try {
            return entity.map(BodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter métricas corporais encontradas para domínio.",
                    exception
            );
        }
    }

    @Override
    public Optional<BodyMetrics> findLatestByUserId(UserId userId) {
        Optional<BodyMetricsEntity> entity;
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(userIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar último registro pelo usuário.", exception);
        }

        try {
            return entity.map(BodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter métricas corporais encontradas para domínio.",
                    exception
            );
        }
    }

    @Override
    public void deleteById(BodyMetricsId bodyMetricsId) {
        try {
            Long userBodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            bodyMetricsJpaRepository.deleteById(userBodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao excluir métricas corporais por id.", exception);
        }
    }
}
