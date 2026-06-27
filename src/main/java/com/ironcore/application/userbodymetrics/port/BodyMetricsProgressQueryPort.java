package com.ironcore.application.userbodymetrics.port;

import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressProjection;
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
