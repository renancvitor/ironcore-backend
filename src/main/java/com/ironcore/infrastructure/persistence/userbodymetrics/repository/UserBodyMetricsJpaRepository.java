package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBodyMetricsJpaRepository extends JpaRepository<UserBodyMetricsEntity, Long> {

    Optional<UserBodyMetricsEntity> findByIdAndUser_Id(Long userBodyMetricsId, Long userId);

    Optional<UserBodyMetricsEntity> findFirstByUser_IdOrderByMeasuredAtDesc(Long userId);

    Page<UserBodyMetricsEntity> findByUser_IdOrderByMeasuredAtDescIdDesc(Long userId, Pageable pageable);
}
