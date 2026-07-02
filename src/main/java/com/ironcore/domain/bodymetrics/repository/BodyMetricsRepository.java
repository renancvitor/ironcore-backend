package com.ironcore.domain.bodymetrics.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

import java.util.Optional;

public interface BodyMetricsRepository {

    BodyMetrics save(BodyMetrics bodyMetrics);

    Optional<BodyMetrics> findById(BodyMetricsId bodyMetricsId);

    Optional<BodyMetrics> findByIdAndPersonId(BodyMetricsId bodyMetricsId, PersonId personId);

    Optional<BodyMetrics> findLatestByPersonId(PersonId personId);

    void deleteById(BodyMetricsId bodyMetricsId);
}
