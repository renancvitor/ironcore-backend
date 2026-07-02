package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
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
    private final PersonJpaRepository personJpaRepository;

    @Override
    public BodyMetrics save(BodyMetrics bodyMetrics) {
        BodyMetricsEntity entity;
        try {
            PersonEntity personReference = personJpaRepository.getReferenceById(
                    bodyMetrics.getPersonId().value()
            );

            entity = Objects.requireNonNull(
                    BodyMetricsMapper.toEntity(bodyMetrics, personReference),
                    "BodyMetrics retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        BodyMetricsEntity saveEntity;
        try {
            saveEntity = Objects.requireNonNull(
                    bodyMetricsJpaRepository.save(entity),
                    "BodyMetricsMapper retornou entidade nula após persistência."
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
            Long bodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findById(bodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id.", exception);
        }

        try {
            return entity.map(BodyMetricsMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter id encontrado para domínio.", exception);
        }
    }

    public Optional<BodyMetrics> findByIdAndPersonId(BodyMetricsId bodyMetricsId, PersonId personId) {
        Optional<BodyMetricsEntity> entity;
        try {
            Long bodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            Long personIdValue = Objects.requireNonNull(
                    personId.value(),
                    "Id da pessoa não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findByIdAndPerson_Id(bodyMetricsIdValue, personIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar métricas corporais por id e pessoa.", exception);
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

    public Optional<BodyMetrics> findLatestByPersonId(PersonId personId) {
        Optional<BodyMetricsEntity> entity;
        try {
            Long personIdValue = Objects.requireNonNull(
                    personId.value(),
                    "Id da pessoa não pode ser nulo."
            );
            entity = bodyMetricsJpaRepository.findFirstByPerson_IdOrderByMeasuredAtDesc(personIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar último registro pela pessoa.", exception);
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
            Long bodyMetricsIdValue = Objects.requireNonNull(
                    bodyMetricsId.value(),
                    "Id das métricas corporais não pode ser nulo."
            );
            bodyMetricsJpaRepository.deleteById(bodyMetricsIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao excluir métricas corporais por id.", exception);
        }
    }
}
