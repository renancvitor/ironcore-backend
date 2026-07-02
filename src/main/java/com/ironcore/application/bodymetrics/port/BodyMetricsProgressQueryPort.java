package com.ironcore.application.bodymetrics.port;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.domain.person.valueobject.PersonId;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricsProgressQueryPort {

    List<BodyMetricsProgressProjection> findProgressData(
            PersonId personId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
