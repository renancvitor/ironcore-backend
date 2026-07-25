package com.ironcore.domain.muscle.musclesubgroup.repository;

import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

import java.util.Optional;

public interface MuscleSubgroupRepository {

    Optional<MuscleSubgroup> findById(MuscleSubgroupId id);

    Optional<MuscleSubgroup> findByCode(MuscleSubgroupCode code);
}
