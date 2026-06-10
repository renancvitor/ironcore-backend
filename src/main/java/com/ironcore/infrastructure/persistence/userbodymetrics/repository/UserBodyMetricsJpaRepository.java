package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBodyMetricsJpaRepository extends JpaRepository<UserBodyMetricsEntity, Long> {

    Optional<UserBodyMetricsEntity> findByIdAndUser_Id(Long userBodyMetricsId, Long userId);

    Optional<UserBodyMetricsEntity> findFirstByUser_IdOrderByMeasuredAtDesc(Long userId);

    List<UserBodyMetricsEntity> findByUser_IdOrderByMeasuredAtDesc(Long userId);
}
