package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.application.bodymetrics.port.ListBodyMetricsQueryPort;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.shared.pagination.PageMapper;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ListBodyMetricsQueryAdapter implements ListBodyMetricsQueryPort {

    private final BodyMetricsJpaRepository bodyMetricsJpaRepository;

    @Override
    public PageResult<ListBodyMetricsItemResult> findByUserIdOrderByMeasuredAtDesc(
            UserId userId,
            PageQuery pageQuery
    ) {
        Page<BodyMetricsEntity> entities;
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
            entities = bodyMetricsJpaRepository
                    .findByUser_IdOrderByMeasuredAtDescIdDesc(userIdValue, pageable);
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao buscar histórico de métricas corporais do usuário.",
                    exception
            );
        }

        try {
            Page<ListBodyMetricsItemResult> result = entities.map(entity -> new ListBodyMetricsItemResult(
                    new BodyMetricsId(entity.getId()),
                    entity.getMeasuredAt(),
                    new BodyWeightKg(entity.getWeightKg()),
                    new BodyHeightCm(entity.getHeightCm()),
                    entity.getNotes()
            ));

            return PageMapper.toPageResult(result);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter histórico de métricas corporais do usuário.",
                    exception
            );
        }
    }
}
