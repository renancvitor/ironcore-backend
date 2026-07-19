package com.ironcore.domain.muscle.musclegroup.repository;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;

import java.util.List;
import java.util.Optional;

public interface MuscleGroupRepository {

    Optional<MuscleGroup> findById(MuscleGroupId id);

    Optional<MuscleGroup> findByCode(MuscleGroupCode code);

    List<MuscleGroup> findAll();
}
