package com.ironcore.domain.exercise.repository;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository {

    Optional<Exercise> findById(ExerciseId id);

    List<Exercise> findByEquipmentTypeId(EquipmentTypeId equipmentTypeId);

    List<Exercise> findByActivityTypeId(ActivityTypeId activityTypeId);

    List<Exercise> findAll();
}
