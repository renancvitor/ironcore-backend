package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.application.userbodymetrics.port.BodyMetricsProgressQueryPort;
import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.domain.user.valueobject.UserId;
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
            UserId userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            return bodyMetricsProgressJpaRepository.findProgressData(
                    userIdValue,
                    startDate,
                    endDate
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar dados de progresso de métricas corporais.", exception);
        }
    }
}
