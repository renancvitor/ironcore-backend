package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.application.bodymetrics.port.BodyMetricsProgressQueryPort;
import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.infrastructure.exception.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class BodyMetricsProgressQueryAdapter implements BodyMetricsProgressQueryPort {

    private final BodyMetricsProgressJpaRepository bodyMetricsProgressJpaRepository;

    @Override
    public List<BodyMetricsProgressProjection> findProgressData(
            PersonId personId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        try {
            Long personIdValue = Objects.requireNonNull(
                    personId.value(),
                    "Id da pessoa não pode ser nulo."
            );
            return bodyMetricsProgressJpaRepository.findProgressData(
                    personIdValue,
                    startDate,
                    endDate
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar dados de progresso de métricas corporais.", exception);
        }
    }
}
