package com.ironcore.application.bodymetrics.port;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricsProgressQueryPort {

    List<BodyMetricsProgressProjection> findProgressData(
            UserId userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
