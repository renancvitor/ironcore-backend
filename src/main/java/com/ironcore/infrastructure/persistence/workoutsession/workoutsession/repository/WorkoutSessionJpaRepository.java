package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionEntity, Long> {

    Optional<WorkoutSessionEntity> findByIdAndPerson_Id(Long id, Long personId);

    Optional<WorkoutSessionEntity> findByPerson_IdAndStatus(Long personId, WorkoutSessionStatus status);

    @Query("""
            SELECT s.person.id FROM WorkoutSessionEntity s
            WHERE s.status = :status
              AND (:afterPersonId IS NULL OR s.person.id > :afterPersonId)
            ORDER BY s.person.id
            """)
    List<Long> findInProgressPersonIdsAfter(
            @Param("status") WorkoutSessionStatus status,
            @Param("afterPersonId") Long afterPersonId,
            Pageable pageable
    );
}
