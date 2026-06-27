package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsItemResult;
import com.ironcore.application.userbodymetrics.port.ListUserBodyMetricsQueryPort;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.shared.pagination.PageMapper;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ListUserBodyMetricsQueryAdapter implements ListUserBodyMetricsQueryPort {

    private final UserBodyMetricsJpaRepository userBodyMetricsJpaRepository;

    @Override
    public PageResult<ListUserBodyMetricsItemResult> findByUserIdOrderByMeasuredAtDesc(
            UserId userId,
            PageQuery pageQuery
    ) {
        Page<UserBodyMetricsEntity> entities;
        try {
            Long userIdValue = Objects.requireNonNull(
                    userId.value(),
                    "Id do usuário não pode ser nulo."
            );
            Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
            entities = userBodyMetricsJpaRepository
                    .findByUser_IdOrderByMeasuredAtDescIdDesc(userIdValue, pageable);
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao buscar histórico de métricas corporais do usuário.",
                    exception
            );
        }

        try {
            Page<ListUserBodyMetricsItemResult> result = entities.map(entity -> new ListUserBodyMetricsItemResult(
                    new UserBodyMetricsId(entity.getId()),
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
