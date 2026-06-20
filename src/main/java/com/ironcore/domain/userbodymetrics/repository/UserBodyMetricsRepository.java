package com.ironcore.domain.userbodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

import java.util.List;
import java.util.Optional;

public interface UserBodyMetricsRepository {

    UserBodyMetrics save(UserBodyMetrics userBodyMetrics);

    Optional<UserBodyMetrics> findById(UserBodyMetricsId userBodyMetricsId);

    Optional<UserBodyMetrics> findByIdAndUserId(UserBodyMetricsId userBodyMetricsId, UserId userId);

    Optional<UserBodyMetrics> findLatestByUserId(UserId userId);

    void deleteById(UserBodyMetricsId userBodyMetricsId);
}
