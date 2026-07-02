package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricsProgressJpaRepository extends Repository<BodyMetricsEntity, Long> {

    @Query("""
            select new com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection(
              m.measuredAt,
              m.weightKg,
              m.fatMassKg,
              m.leanMassKg,
              m.bodyFatPercentage,
              m.bmi,
              m.neckCm,
              m.chestCm,
              m.shoulderCm,
              m.armCm,
              m.forearmCm,
              m.waistCm,
              m.hipCm,
              m.thighCm,
              m.calfCm
          )
          from BodyMetricsEntity m
          where m.user.id = :userId
            and m.measuredAt between :startDate and :endDate
          order by m.measuredAt asc, m.id asc
          """)
    List<BodyMetricsProgressProjection> findProgressData(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
