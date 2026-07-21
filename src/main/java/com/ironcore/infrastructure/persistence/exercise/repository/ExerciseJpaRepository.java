package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, Long> {

    List<ExerciseEntity> findByEquipmentType_Id(Long equipmentTypeId);

    List<ExerciseEntity> findByActivityType_Id(Long activityTypeId);
}
