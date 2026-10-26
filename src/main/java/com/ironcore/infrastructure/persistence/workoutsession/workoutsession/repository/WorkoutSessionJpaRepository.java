package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionEntity, Long> {
    Optional<WorkoutSessionEntity> findByIdAndPerson_Id(Long id, Long personId);
    Optional<WorkoutSessionEntity> findByPerson_IdAndStatus(Long personId, WorkoutSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkoutSessionEntity s where s.id = :id and s.person.id = :personId")
    Optional<WorkoutSessionEntity> findOwnedForUpdate(@Param("id") Long id, @Param("personId") Long personId);
}
