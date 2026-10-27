package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionEntity, Long> {

    Optional<WorkoutSessionEntity> findByIdAndPerson_Id(Long id, Long personId);

    Optional<WorkoutSessionEntity> findByPerson_IdAndStatus(Long personId, WorkoutSessionStatus status);
}
