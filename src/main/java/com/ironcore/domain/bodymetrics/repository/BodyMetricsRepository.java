package com.ironcore.domain.bodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

import java.util.Optional;

public interface BodyMetricsRepository {

    BodyMetrics save(BodyMetrics bodyMetrics);

    Optional<BodyMetrics> findById(BodyMetricsId bodyMetricsId);

    Optional<BodyMetrics> findByIdAndUserId(BodyMetricsId bodyMetricsId, UserId userId);

    Optional<BodyMetrics> findLatestByUserId(UserId userId);

    void deleteById(BodyMetricsId bodyMetricsId);
}
