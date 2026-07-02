package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyMetricsJpaRepository extends JpaRepository<BodyMetricsEntity, Long> {

    Optional<BodyMetricsEntity> findByIdAndUser_Id(Long userBodyMetricsId, Long userId);

    Optional<BodyMetricsEntity> findFirstByUser_IdOrderByMeasuredAtDesc(Long userId);

    Page<BodyMetricsEntity> findByUser_IdOrderByMeasuredAtDescIdDesc(Long userId, Pageable pageable);
}
