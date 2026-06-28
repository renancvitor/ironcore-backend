package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricsProgressJpaRepository extends Repository<UserBodyMetricsEntity, Long> {

    @Query("""
            select new com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressProjection(
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
          from UserBodyMetricsEntity m
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
