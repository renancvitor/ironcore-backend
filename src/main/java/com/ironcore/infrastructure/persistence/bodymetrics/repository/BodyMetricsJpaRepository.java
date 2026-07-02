package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyMetricsJpaRepository extends JpaRepository<BodyMetricsEntity, Long> {

    Optional<BodyMetricsEntity> findByIdAndPerson_Id(Long bodyMetricsId, Long personId);

    Optional<BodyMetricsEntity> findFirstByPerson_IdOrderByMeasuredAtDesc(Long personId);

    Page<BodyMetricsEntity> findByPerson_IdOrderByMeasuredAtDescIdDesc(Long personId, Pageable pageable);
}
